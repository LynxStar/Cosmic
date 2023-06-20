package net.encryption;

import constants.net.ServerConstants;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

class MapleAESTest {

    @Test
    void basicTest() {

        var sendIv = InitializationVector.generateSend();

        var innerBytes = sendIv.getBytes();
        innerBytes[3] = 17;

        var version = (short) (0xFFFF - ServerConstants.VERSION);

        var mapleAES = new MapleAESOFB(sendIv, version);

        var bytes = new byte[]{ 10, 20, 30, 40, 50, 60  };

        mapleAES.crypt(bytes);

        var ints = new int[mapleAES.iv.length];

        for(var i = 0; i < mapleAES.iv.length; i++)
        {

            var c = (int)mapleAES.iv[i];

            ints[i] = c < 0
                    ? c + 256
                    : c
                    ;
        }

    }

}
