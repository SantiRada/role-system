package Tenzinn.UI.Events;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class BasicEvent {

    private String action;

    public BasicEvent() { }

    public String getAction() { return action; }

    public static final BuilderCodec<BasicEvent> CODEC = BuilderCodec
            .builder(BasicEvent.class, BasicEvent::new).append(new KeyedCodec<>("Action", Codec.STRING), (data, value) -> data.action = value,
                    (data) -> data.action).add().build();
}