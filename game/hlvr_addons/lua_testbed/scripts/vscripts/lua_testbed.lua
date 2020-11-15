function clearConsole()
  SendToConsole("clear")
end

function printPlayerCoords()
  local playerCoords = Entities:FindByClassname(nil, "player"):GetCenter()
  Msg(playerCoords[1]..","..playerCoords[2]..","..playerCoords[3]);
end

function moveReceivedCoords()
  (Entities:FindByName(nil, "moveEnt")):SetOrigin(require("lua_testbed_io"))
end

function reloadCoords()
  SendToConsole("cl_script_reload")
end
