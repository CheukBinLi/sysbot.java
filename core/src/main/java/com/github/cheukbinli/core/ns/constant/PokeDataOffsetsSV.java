package com.github.cheukbinli.core.ns.constant;

public class PokeDataOffsetsSV {

    public static final String ScarletID = "0100A3D008C5C000";
    public static final String VioletID = "01008F6008C5E000";
    public static final long[] BoxStartPokemonPointer = new long[]{0x4384B18, 0x128, 0x9B0, 0x0};
    public static final int BOX_SLOT_SIZE = 0x158;
    public static final long[] LinkTradePartnerPokemonPointer = new long[]{0x437ECE0, 0x48, 0x58, 0x40, 0x148};
    public static final long[] LinkTradePartnerNIDPointer = new long[]{0x43A28F0, 0xF8, 0x8};
    public static final long[] MyStatusPointer = new long[]{0x4384B18, 0x148, 0x40};
    public static final long[] Trader1MyStatusPointer = new long[]{0x437ECE0, 0x48, 0xB0, 0x0}; // The trade partner status uses a compact struct that looks like MyStatus.
    public static final long[] Trader2MyStatusPointer = new long[]{0x437ECE0, 0x48, 0xE0, 0x0};
    public static final long[] ConfigPointer = new long[]{0x4384B18, 0x1B8, 0x40};
    public static final long[] CurrentBoxPointer = new long[]{0x4384B18, 0x120, 0x570};
    public static final long[] PortalBoxStatusPointer = new long[]{0x439DFF0, 0x18, 0xA0, 0x1B8, 0x70, 0x28};  // 9-A in portal, 4-6 in box.
    public static final long[] IsConnectedPointer = new long[]{0x437E280, 0x30};
    public static final long[] OverworldPointer = new long[]{0x43A7848, 0x348, 0x10, 0xD8, 0x28};

    public static final int BoxFormatSlotSize = 0x158;
    public static final long LibAppletWeID = 0x010000000000100aL; // One of the process IDs for the news.

    public static final int TradeMyStatusInfoLength = 0x30;
    public static final int FAKE_TRAINER_SAV_INFO_LENGTH = 104;

//    public static final long[] TextSpeedPointer =new long[]{ 0x42BA6B0, 0x1E0, 0x68 };

    public static void main(String[] args) {
        System.out.println(LibAppletWeID);
        System.out.println(BoxStartPokemonPointer[0]);
    }

}
