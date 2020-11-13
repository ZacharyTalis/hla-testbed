import com.esotericsoftware.kryonet.*;
import com.esotericsoftware.kryo.*;
import packets.*;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class lua_testbed {
    public static void main(String[] args)  {

        Properties properties = readProperties(args[0]);

        Server server = startServer(
                Integer.parseInt((String)properties.get("server_tcpPort")),
                Integer.parseInt((String)properties.get("server_udpPort")));
        assert server != null;
        addServerListener(server, args[0]);

        CoordsRequest request = new CoordsRequest();
        request.text = "1,2,6";

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

        boolean loopFailure = false;
        while (!loopFailure) {
            try {
                client.sendTCP(request);
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                loopFailure = true;
            }
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
//                    System.out.print("CoordsRequest received: ");
//                    System.out.println(request.text);
                    writeLua(request.text, filepath);

                    CoordsResponse response = new CoordsResponse();
//                    response.text = "CoordsResponse text.";
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
//                    System.out.println("CoordsResponse received.");
                }
            }
        });
    }

    private static void writeLua (String text, String filepath) {
        try {

            File luaFile = new File(filepath+"\\lua_testbed_io.lua");
//            if (!luaFile.createNewFile()) System.out.println("Lua file already exists! Writing anyways.");

            FileWriter luaWrite = new FileWriter(filepath+"\\lua_testbed_io.lua");
            luaWrite.write("return Vector3("+text+")");
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

}
