map_write(map, CMD(0xd0), adr);
/* Make sure we're in 'read status' mode if it had finished */
map_write(map, CMD(0x70), adr);
chip->state = FL_ERASING;
chip->oldstate = FL_READY;