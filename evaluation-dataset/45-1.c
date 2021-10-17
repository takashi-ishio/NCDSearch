 if(cifs_sb->mnt_cifs_flags & CIFS_MOUNT_NO_BRL) 
    tmp_inode->i_fop->lock = NULL; 
