 priv->tx_power_next = tx_power; 
 if (test_bit(STATUS_SCANNING, &priv->status) && !force) { 
    IWL_DEBUG_INFO(priv, "Deferring tx power set while scanning\n"); 
    return 0; 
