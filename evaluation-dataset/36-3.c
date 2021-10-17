 result = usb_submit_urb(priv->bulk_read_urb, GFP_ATOMIC); 
 if (result) dev_err(&port->dev,            
"%s  failed submitting read urb, error %d\n", 
