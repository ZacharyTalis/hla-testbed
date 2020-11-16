import com.esotericsoftware.kryonet.*;
import com.esotericsoftware.kryo.*;
import packets.*;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class lua_testbed {
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args)  {

        Properties properties = readProperties(args[0]);

        Server server = startServer(
                Integer.parseInt((String)properties.get("server_tcpPort")),
                Integer.parseInt((String)properties.get("server_udpPort")));
        assert server != null;
        addServerListener(server, args[0]);

        CoordsRequest request = new CoordsRequest();
        request.text = "1,2,7";

        Client client = startClient(
                Integer.parseInt((String)properties.get("client_timeout")),
                (String)properties.get("client_ip"),
                Integer.parseInt((String)properties.get("client_tcpPort")),
                Integer.parseInt((String)properties.get("client_udpPort")));
        assert client != null;
        addClientListener(client);

        Kryo serverKryo = server.getKryo();
        serverKryo.register(CoordsRequest.class);
        serverKryo.register(CoordsResponse.class);

        Kryo clientKryo = client.getKryo();
        clientKryo.register(CoordsRequest.class);
        clientKryo.register(CoordsResponse.class);
        String output;

        try {
            while (true) {
                TimeUnit.MILLISECONDS.sleep(1);
                output = readConsoleLog((String)properties.get("log_path"));
                if (output != null) {
                    request.text = output;
                    System.out.println(output);
                    client.sendTCP(request);
                }
                TimeUnit.MILLISECONDS.sleep(9);
            }
        } catch (Exception e) {
                e.printStackTrace();
        }
    }

    private static Server startServer(int tcpPort, int udpPort) {
        try {
            Server server = new Server();
            server.start();
            server.bind(tcpPort, udpPort);
            return server;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
    }

    private static void addServerListener(Server server, String filepath) {
        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof CoordsRequest) {
                    CoordsRequest request = (CoordsRequest)object;
                    writeLua(request.text, filepath);

                    CoordsResponse response = new CoordsResponse();
                    connection.sendTCP(response);
                }
            }
        });
    }

    private static Client startClient(int timeout, String ip, int tcpPort, int udpPort) {
        Client client = new Client();
        client.start();
        try {
            client.connect(timeout, ip, tcpPort, udpPort);
            return client;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
    }

    private static void addClientListener(Client client) {
        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof CoordsResponse) {
                    CoordsResponse response = (CoordsResponse)object;
                }
            }
        });
    }

    private static void writeLua (String text, String filepath) {
        try {

            File luaFile = new File(filepath+"\\lua_testbed_io.lua");

            FileWriter luaWrite = new FileWriter(filepath+"\\lua_testbed_io.lua");
            luaWrite.write("(Entities:FindByName(nil, \"moveEnt\")):SetOrigin(Vector("+text+"))");
            luaWrite.close();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static Properties readProperties (String filepath) {
        Properties properties = new Properties();

        try {
            InputStream file = new FileInputStream(filepath+"\\config.txt");
            properties.load(file);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return properties;
    }

    private static void clearConsoleLog(String filepath) {
        try {
            PrintWriter clear = new PrintWriter(filepath);
            clear.close();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }

    private static String readConsoleLog(String filepath) {
        try {
            Scanner scanner = new Scanner(new File(filepath));
            String output = scanner.nextLine();
            clearConsoleLog(filepath);
            output =  output.replace("\0","");
            if (output.matches("(-?\\d+(?:\\.\\d+)?),(-?\\d+(?:\\.\\d+)?),(-?\\d+(?:\\.\\d+)?)")) return output;
            return null;
        } catch (NoSuchElementException noSuchElementException) {
            return null;
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            return null;
        }
    }

}
