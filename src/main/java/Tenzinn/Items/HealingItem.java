package Tenzinn.Items;

public enum HealingItem {

    BANDAGE    ("Bandage",    false),
    MEDICAL_KIT("MedicalKit", true);

    public final String  itemId;
    public final boolean medicOnly;

    HealingItem(String itemId, boolean medicOnly) {
        this.itemId     = itemId;
        this.medicOnly  = medicOnly;
    }
    public float getHealSelfPercent(boolean isMedic) {
        switch (this) {
            case BANDAGE:     return isMedic ? 0.20f : 0.05f;
            case MEDICAL_KIT: return 1.00f;
            default:          return 0f;
        }
    }
    public float getHealOtherPercent() {
        switch (this) {
            case BANDAGE:     return 0.20f;
            case MEDICAL_KIT: return 1.00f;
            default:          return 0f;
        }
    }
    public int getTimeToHeal(boolean isMedic) {
        switch (this) {
            case BANDAGE:     return isMedic ? 5 : 10;
            case MEDICAL_KIT: return 20;
            default:          return 5;
        }
    }
    public static HealingItem fromItemId(String itemId) {
        for (HealingItem item : values()) { if (item.itemId.equals(itemId)) return item; }
        return null;
    }
}