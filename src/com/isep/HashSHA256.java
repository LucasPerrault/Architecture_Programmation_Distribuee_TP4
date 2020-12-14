package com.isep;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HashSHA256 {
    private final static String ALGORITHM_SHA256 = "SHA-256";
    private final static String STANDARD_CHAR_SET = "UTF-8";

    private byte[] hash;
    private int hashSize;
    private MessageDigest algorithmSHA256;

    public HashSHA256(String s) {
        this.initAlgorithm();
        byte[] leafBytes = this.parseMessageStringToLeafBytes(s);
        hash = algorithmSHA256.digest(leafBytes);
    }

    public HashSHA256(HashSHA256 hashLeft, HashSHA256 hashRight) {
        this.initAlgorithm();
        byte[] hashMerged = new byte[1 + hashLeft.hashSize + hashRight.hashSize];
        hashMerged[0] = 0x01;
        System.arraycopy(hashLeft.hash, 0, hashMerged, 1, hashSize);
        System.arraycopy(hashRight.hash, 0, hashMerged, 1 + hashSize, hashSize);
        hash = algorithmSHA256.digest(hashMerged);
    }

    @Override
    public String toString() {
        return Arrays.toString(hash);
    }

    public byte[] getHash() {
        return hash;
    }

    public int getHashSize() {
        return hashSize;
    }

    /* Initialize the algorithm SHA256 & the HASH */
    private void initAlgorithm() {
        try {
            algorithmSHA256 = MessageDigest.getInstance(ALGORITHM_SHA256);
            hashSize = algorithmSHA256.getDigestLength();
            hash = new byte[hashSize];
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("Not Found. We didn't find algorithm named as " + ALGORITHM_SHA256);
            System.exit(1);
        }
    }

    /* Parse message string to leaf bytes */
    private byte[] parseMessageStringToLeafBytes(String message) {
        try {
            byte[] messageBytes = message.getBytes(STANDARD_CHAR_SET);
            int messageBytesLength = messageBytes.length;
            byte[] leafBytes = new byte[messageBytesLength + 1];
            leafBytes[0] = 0x00;
            System.arraycopy(messageBytes, 0, leafBytes, 1, messageBytesLength);
            return leafBytes;
        } catch(UnsupportedEncodingException e) {
            System.out.println("Encoding Error. We didn't succeed to encode message to bytes.");
            System.exit(1);
            return null;
        }
    }
}
