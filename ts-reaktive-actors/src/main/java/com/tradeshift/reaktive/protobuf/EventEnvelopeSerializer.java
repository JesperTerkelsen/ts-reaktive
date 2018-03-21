package com.tradeshift.reaktive.protobuf;

import com.google.protobuf.ByteString;

import akka.actor.ActorSystem;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.Sequence;
import akka.persistence.query.TimeBasedUUID;
import akka.serialization.Serialization;
import akka.serialization.SerializationExtension;

/**
 * Serializes instances of {@link akka.persistence.query.EventEnvelope},
 * into the protobuf {@link EventEnvelope}. In order to do this, it reuses akka's serialization mechanism.
 */
public class EventEnvelopeSerializer {
    private final Serialization ext;

    /**
     * Creates a new EventEnvelopeSerializer.
     */
    public EventEnvelopeSerializer(ActorSystem system) {
        this.ext = SerializationExtension.get(system);
    }

    public Query.EventEnvelope toProtobuf(EventEnvelope e) {
        ByteString event;
        if (e.event() instanceof ByteString) {
            event = (ByteString) e.event();
        } else if (e.event() instanceof byte[]) {
            event = ByteString.copyFrom((byte[]) e.event());
        } else {
            event = ByteString.copyFrom(ext.serialize(e.event()).get());
        }
        
        long timestamp = (e.offset() instanceof Sequence) 
			? Sequence.class.cast(e.offset()).value()
			: com.tradeshift.reaktive.akka.UUIDs.unixTimestamp(TimeBasedUUID.class.cast(e.offset()).value());
			
        return Query.EventEnvelope.newBuilder()
            .setPersistenceId(e.persistenceId())
            .setTimestamp(timestamp)
            .setSequenceNr(e.sequenceNr())
            .setEvent(event)
            .build();
    }
}
