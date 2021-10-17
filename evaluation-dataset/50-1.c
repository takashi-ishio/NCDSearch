 i810_writel(mmio, chan->ddc_base, (state ? SCL_VAL_OUT : 0) | 
    SCL_DIR | SCL_DIR_MASK | SCL_VAL_MASK); 
 i810_readl(mmio, chan->ddc_base); 