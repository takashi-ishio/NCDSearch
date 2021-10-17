 i810_writel(mmio, chan->ddc_base, (state ? SDA_VAL_OUT : 0) | 
   SDA_DIR |  SDA_DIR_MASK | SDA_VAL_MASK); 
 i810_readl(mmio, chan->ddc_base);   
