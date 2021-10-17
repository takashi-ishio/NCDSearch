result = usb_submit_urb(priv->bulk_read_urb, GFP_KERNEL); 
if (result)  dev_err(&port->dev,  
   "%s  failed resubmitting read urb,
 error %d\n",__func__, result); 
