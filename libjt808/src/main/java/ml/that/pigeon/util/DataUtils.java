package ml.that.pigeon.util;

/**
 * Created by DeW on 2017/3/31.
 */

public class DataUtils {

    /**
     * transfer manufacturer id into byte[5];
     * @param id
     * @return
     */
    public static byte[] getPhoneBytes(long id){
        if(id > 1099511627775L){
            throw new IllegalArgumentException("id is bigger than 1099511627775L");
        }
        byte[] idBytes = new byte[5];
        idBytes[0] = (byte) ((id>>32) & 0xFF);
        idBytes[1] = (byte) ((id>>24) & 0xFF);
        idBytes[2] = (byte) ((id>>16) & 0xFF);
        idBytes[3] = (byte) ((id>>8) & 0xFF);
        idBytes[4] = (byte) (id & 0xFF);
        return idBytes;
    }

}
