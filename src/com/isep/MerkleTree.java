package com.isep;

public class MerkleTree {

    private HashSHA256 hashSHA256;
    private MerkleTree merkleTreeLeft, merkleTreeRight;
    private int startRangeLog, endRangeLog, size, next;

    public MerkleTree(String leafToString, int index) {
        size = 1;
        next = 1;
        startRangeLog = index;
        endRangeLog = index;
        this.hashSHA256 = new HashSHA256(leafToString);
    }

    public MerkleTree(MerkleTree left, MerkleTree right) {
        if (left.endRangeLog != right.startRangeLog - 1) {
            System.out.println("Conflict error. Merkle Trees are not contiguous.");
            System.out.println("MerkleTree Right start at " + (right.startRangeLog - 1)
                    + "& MerkleTree Left end at " + left.endRangeLog);
            System.exit(1);
            return;
        }

        this.merkleTreeLeft = left;
        this.merkleTreeRight = right;

        startRangeLog = left.startRangeLog;
        endRangeLog = right.endRangeLog;
        size = merkleTreeLeft.size + merkleTreeRight.size;

        this.updateNext();

        this.hashSHA256 = new HashSHA256(left.hashSHA256, right.hashSHA256);
    }

    private void updateNext() {
        if(size < Math.max(merkleTreeLeft.next, merkleTreeRight.next)) {
            next = Math.max(merkleTreeLeft.next, merkleTreeRight.next);
        } else {
            next = 2 * Math.max(merkleTreeLeft.next, merkleTreeRight.next);
        }
    }

    public int getEndRangeLog() {
        return endRangeLog;
    }

    public int getStartRangeLog() {
        return startRangeLog;
    }

    public HashSHA256 getHashSHA256() {
        return hashSHA256;
    }

    public int getSize() {
        return size;
    }

    public MerkleTree getMerkleTreeLeft() {
        return merkleTreeLeft;
    }

    public MerkleTree getMerkleTreeRight() {
        return merkleTreeRight;
    }

    public int getNext() {
        return next;
    }

    @Override
    public String toString() {
        return "hash: " + getHashSHA256();
    }
}
