map_write(map, cfi->sector_erase_cmd, chip->in_progress_block_addr); 
chip->state = FL_ERASING;
chip->oldstate = FL_READY;