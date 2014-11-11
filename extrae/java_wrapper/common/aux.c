
generate_communication_event(jboolean send, jlong tag, jlong size, jlong partner, jlong id) {
      struct extrae_UserCommunication comm;
        struct extrae_CombinedEvents events;

        Extrae_init_UserCommunication(&comm);
        Extrae_init_CombinedEvents(&events);

        if(send)
                comm.type = EXTRAE_USER_SEND;
        else
                comm.type = EXTRAE_USER_RECV;

        comm.tag=tag;
        comm.size=size;
        comm.partner=partner;
        comm.id=id;

        events.nCommunications=1;
        events.Communications=&comm;
        events.nEvents=0;

        Extrae_emit_CombinedEvents(&events);
        printf("Event emit done\n");
}
