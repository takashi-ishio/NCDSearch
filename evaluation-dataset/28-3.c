map_write(map, CMD(LPDDR_RESUME), 
          map->pfow_base + PFOW_COMMAND_CODE);
map_write(map, CMD(LPDDR_START_EXECUTION),
          map->pfow_base + PFOW_COMMAND_EXECUTE);
chip->state = FL_ERASING;
chip->oldstate = FL_READY;
