if (buffer_info->dma) { 
  pci_unmap_page(adapter->pdev, buffer_info->dma, 
                              buffer_info->length, PCI_DMA_TODEVICE); 
  buffer_info->dma = 0; 
} 
