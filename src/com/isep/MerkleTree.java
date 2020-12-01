package com.isep;

public class MerkleTree {

    HashSHA256 hashSHA256;
    MerkleTree merkleTreeLeft, merkleTreeRight;
    int startRangeLog, endRangeLog, size, next;

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
            System.out.println("MerkleTree Right start at" + (right.startRangeLog - 1)
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

}
