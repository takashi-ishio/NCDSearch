if (reg & 0xff00) { 
  outb_p(W83781D_REG_BANK, 
  data->addr + W83781D_ADDR_REG_OFFSET); 
  outb_p(0, data->addr + W83781D_DATA_REG_OFFSET);   } 
