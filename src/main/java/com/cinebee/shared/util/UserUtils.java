package com.cinebee.shared.util;

public class UserUtils {
    /**
     * Remove Vietnamese diacritics from a string for username generation.
     * @param str Input string
     * @return String without Vietnamese tones
     */
    public static String removeVietnameseTones(String str) {
        str = str.replaceAll("[Ã Ã¡áº¡áº£Ã£Ã¢áº§áº¥áº­áº©áº«Äƒáº±áº¯áº·áº³áºµ]", "a");
        str = str.replaceAll("[Ã¨Ã©áº¹áº»áº½Ãªá»áº¿á»‡á»ƒá»…]", "e");
        str = str.replaceAll("[Ã¬Ã­á»‹á»‰Ä©]", "i");
        str = str.replaceAll("[Ã²Ã³á»á»ÃµÃ´á»“á»‘á»™á»•á»—Æ¡á»á»›á»£á»Ÿá»¡]", "o");
        str = str.replaceAll("[Ã¹Ãºá»¥á»§Å©Æ°á»«á»©á»±á»­á»¯]", "u");
        str = str.replaceAll("[á»³Ã½á»µá»·á»¹]", "y");
        str = str.replaceAll("Ä‘", "d");
        str = str.replaceAll("[Ã€Ãáº áº¢ÃƒÃ‚áº¦áº¤áº¬áº¨áºªÄ‚áº°áº®áº¶áº²áº´]", "A");
        str = str.replaceAll("[ÃˆÃ‰áº¸áººáº¼ÃŠá»€áº¾á»†á»‚á»„]", "E");
        str = str.replaceAll("[ÃŒÃá»Šá»ˆÄ¨]", "I");
        str = str.replaceAll("[Ã’Ã“á»Œá»ŽÃ•Ã”á»’á»á»˜á»”á»–Æ á»œá»šá»¢á»žá» ]", "O");
        str = str.replaceAll("[Ã™Ãšá»¤á»¦Å¨Æ¯á»ªá»¨á»°á»¬á»®]", "U");
        str = str.replaceAll("[á»²Ãá»´á»¶á»¸]", "Y");
        str = str.replaceAll("Ä", "D");
        str = str.replaceAll("[^a-zA-Z0-9]", "");
        return str;
    }
}

