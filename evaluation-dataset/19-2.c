if (object->type == tag_type) {
	struct tag *tag = (struct tag *) object;
	object->flags |= flags;
	if (tag_objects && !(object->flags & UNINTERESTING))
		add_pending_object(object, tag->tag);
	object = tag->tagged; /* Ishio: Tech-repot says only the lines 1 and 6 are related to the bug. */
