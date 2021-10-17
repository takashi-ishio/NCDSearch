rt2x00_set_field32(&reg, CSR14_TSF_COUNT, 1); 
rt2x00_set_field32(&reg, CSR14_TBCN, (conf->sync == TSF_SYNC_BEACON)); 
rt2x00_set_field32(&reg, CSR14_BEACON_GEN, 0); 
