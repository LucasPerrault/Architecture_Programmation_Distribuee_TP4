package com.isep;

import java.util.LinkedList;

public class Auditor {
    HashSHA256 rootHash;
    LogServer server;

    public Auditor(LogServer server) {
        rootHash = server.merkleTree.getHashSHA256();
        this.server = server;
    }

    public LogServer getServer() {
        return server;
    }

    public HashSHA256 getRootHash() {
        return rootHash;
    }

    public boolean isMember(String event, int index) {
        LinkedList<HashSHA256> path = server.genPath(index);
        HashSHA256 hash = buildHash(event, index, server.merkleTree.getEndRangeLog(), path);
        return hash.equals(rootHash);
    }

    public HashSHA256 buildHash(String event, int index, int end, LinkedList<HashSHA256> path) {
        if (end == 0) {
            if (path.size() != 0) {
                System.out.println("hash list not empty!");
            }
            return new HashSHA256(event);
        }
        int middle = greatestPowerTwoSmaller(end);

        if (index < middle) {
            HashSHA256 right = path.removeLast();
            HashSHA256 left = buildHash(event, index, middle - 1, path);
            return new HashSHA256(left, right);
        }
        HashSHA256 left = path.removeLast();
        HashSHA256 right = buildHash(event, index - middle, end - middle, path);
        return new HashSHA256(left, right);
    }


    public boolean isConsistent(LogServer newLogServer) {
        if (server.merkleTree.getSize() > newLogServer.merkleTree.getSize())
            return false;
        if (server.merkleTree.getSize() == newLogServer.merkleTree.getSize()) {
            if (rootHash.equals(newLogServer.currentRootHashSHA256())) {
                return true;
            }
            return false;
        }

        int index = server.merkleTree.getEndRangeLog();
        LinkedList<HashSHA256> path = newLogServer.genProof(index);

        int end = newLogServer.merkleTree.getEndRangeLog();

        while (index % 2 != 0) {
            end /= 2;
            index /= 2;
        }

        HashSHA256 hash = path.removeFirst();
        hash = buildProofHash(hash, index, end, path);

        return hash.equals(rootHash);
    }

    public HashSHA256 buildProofHash(HashSHA256 hash, int index, int end, LinkedList<HashSHA256> path) {
        if (end == 0) {
            if (path.size() != 0) {
                System.out.println("hash list not empty!");
            }
            return hash;
        }
        int middle = greatestPowerTwoSmaller(end);

        if (index < middle) {
            HashSHA256 removed = path.removeLast();
            return buildProofHash(hash, index, middle - 1, path);
        }
        HashSHA256 left = path.removeLast();
        HashSHA256 right = buildProofHash(hash, index - middle, end - middle, path);
        return new HashSHA256(left, right);
    }

    public int greatestPowerTwoSmaller(int index) {
        return Integer.highestOneBit(index);
    }

}
