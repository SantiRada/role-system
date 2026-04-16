package Tenzinn.Interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;

public class MedicalBenchComponent {

    public static final BuilderCodec<MedicalBenchComponent> CODEC = BuilderCodec.builder(MedicalBenchComponent.class, MedicalBenchComponent::new).build();

    public boolean isUnlocked = false;
}