function clearConsole()
  SendToConsole("clear")
end

function printPlayerCoords()
  local playerCoords = Entities:FindByClassname(nil, "player"):GetCenter()
  Msg(playerCoords[1]..","..playerCoords[2]..","..playerCoords[3]);
end

function reloadCoords()
  SendToConsole("script_reload_code lua_testbed_io")
end
