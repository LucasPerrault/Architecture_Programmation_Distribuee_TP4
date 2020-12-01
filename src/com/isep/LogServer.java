package com.isep;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Scanner;

public class LogServer {
    MerkleTree merkleTree;

    public LogServer(MerkleTree merkleTree) {
        this.merkleTree = merkleTree;
    }

    /* Define a constructor that reads a full log from a text file to construct the tree */
    public LogServer(String inputTextFile) {
        Queue<MerkleTree> merkleQueue = new LinkedList<MerkleTree>();
        try {
            Scanner input = new Scanner(new FileReader(inputTextFile));
            int rangeInputIndex = 0;
            while(input.hasNextLine()) {
                merkleQueue.add(new MerkleTree(input.nextLine(), rangeInputIndex));
                rangeInputIndex++;
            }
            buildTree(merkleQueue);
        } catch(FileNotFoundException e) {
            System.out.println("FileNotFoundException. No such file found.");
            System.exit(1);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public HashSHA256 currentRootHashSHA256() {
        return this.merkleTree.hashSHA256;
    }

    public LinkedList<HashSHA256> genPath(int rangeIndex) {
        return genPathRecursive(rangeIndex, merkleTree, new LinkedList<HashSHA256>());
    }

    public LinkedList<HashSHA256> genProof(int rangeIndex){
        return genProofRecursive(rangeIndex, merkleTree, new LinkedList<HashSHA256>());
    }

    public void append(String log) {
        merkleTree = appendSingleByRecursivity(log, merkleTree, merkleTree.size);
    }

    public void append(LinkedList<String> list) {
        merkleTree = appendManyByRecursivity(list, merkleTree, merkleTree.size);
    }

    /* Build one single MerkleTree with many MerkleTrees */
    private void buildTree(Queue<MerkleTree> merkleQueueInit) {
        if (merkleQueueInit.size() == 0) {
            System.out.println("Sorry but the queue is empty..");
            return;
        }

        Queue<MerkleTree>[] merkleQueues = new LinkedList[2];
        merkleQueues[0] = merkleQueueInit;
        merkleQueues[1] = new LinkedList<MerkleTree>();

        /* A REVOIR */
        int a = 0;
        int b = 1;
        while(merkleQueues[a].size() != 1) {
            MerkleTree left, right;
            while(merkleQueues[a].size() > 0) {
                left = merkleQueues[a].poll();
                right = merkleQueues[a].poll();
                if(right == null) merkleQueues[b].add(left);
                else merkleQueues[b].add(new MerkleTree(left, right));
            }
            a = (a+1) % 2;
            b = (b+1) % 2;
        }
        merkleTree = merkleQueues[a].poll();
    }

    /* Make the audit path with recursivity */
    private LinkedList<HashSHA256> genPathRecursive(int rangeIndex, MerkleTree m, LinkedList<HashSHA256> path) {
        boolean hasSameIndex = m.startRangeLog == m.endRangeLog && m.endRangeLog == rangeIndex;
        if (hasSameIndex) {
            return path;
        }

        boolean hasTreeLeftSuperiorEnd = m.merkleTreeLeft != null && m.merkleTreeLeft.endRangeLog >= rangeIndex;
        if (hasTreeLeftSuperiorEnd) {
            path.addFirst(m.merkleTreeRight.hashSHA256);
            return genPathRecursive(rangeIndex, m.merkleTreeLeft, path);
        }

        boolean hasTreeRightSuperiorEnd = m.merkleTreeRight != null && m.merkleTreeRight.endRangeLog >= rangeIndex;
        if (hasTreeRightSuperiorEnd) {
            path.addFirst(Objects.requireNonNull(m.merkleTreeLeft).hashSHA256);
            return genPathRecursive(rangeIndex, m.merkleTreeRight, path);
        }

        return path;
    }

    /* Make the proof with recursivity */
    private LinkedList<HashSHA256> genProofRecursive(int rangeIndex, MerkleTree m, LinkedList<HashSHA256> proof) {
        if(m.endRangeLog < rangeIndex || m.startRangeLog > rangeIndex) {
            return proof;
        }

        if (m.endRangeLog == rangeIndex){
            proof.addFirst(m.hashSHA256);
            return proof;
        }

        if (m.merkleTreeLeft != null && m.merkleTreeLeft.endRangeLog < rangeIndex) {
            proof.addFirst(m.merkleTreeLeft.hashSHA256);
            return genProofRecursive(rangeIndex, m.merkleTreeRight, proof);
        }

        if (m.merkleTreeLeft != null) {
            proof.addFirst(m.merkleTreeRight.hashSHA256);
            proof = genProofRecursive(rangeIndex, m.merkleTreeLeft, proof);
            return proof;
        }

        return null;
    }

    /* Append single by recursivity */
    private MerkleTree appendSingleByRecursivity(String log, MerkleTree m, int rank) {
        if(m == null) {
            return new MerkleTree(log, 0);
        }

        if(m.size == m.next) {
            MerkleTree newElement = new MerkleTree(log, rank);
            return new MerkleTree(m, newElement);
        }

        MerkleTree merkleTreeRightCurrent = appendSingleByRecursivity(log, m.merkleTreeRight, rank);
        return new MerkleTree(m.merkleTreeLeft, merkleTreeRightCurrent);
    }

    /* Append many by recursivity */
    private MerkleTree appendManyByRecursivity(LinkedList<String> logs, MerkleTree m, int rank) {
        if (logs.isEmpty()) {
            return m;
        }

        if (m == null){
            m = new MerkleTree(logs.poll(), 0);
            return appendManyByRecursivity(logs, m, rank+1);
        }

        int numberOfReadLog = m.next - m.size;
        MerkleTree merkleTreeRes;
        if(numberOfReadLog == 0){
            merkleTreeRes = new MerkleTree(m, new MerkleTree(logs.poll(), rank));
            return appendManyByRecursivity(logs, merkleTreeRes, rank + 1);
        }

        if(logs.size() > numberOfReadLog){
            LinkedList<String> elements = logs;
            LinkedList<String> completeTreeElements = new LinkedList<String>();

            for(int i = 0; i < numberOfReadLog; i++) completeTreeElements.add(elements.poll());

            merkleTreeRes = new MerkleTree(m.merkleTreeLeft, appendManyByRecursivity(completeTreeElements, m.merkleTreeRight, rank));
            merkleTreeRes = new MerkleTree(merkleTreeRes, new MerkleTree(elements.poll(), rank + numberOfReadLog));
            MerkleTree merkleTreeRightCurrent = appendManyByRecursivity(elements, merkleTreeRes.merkleTreeRight, rank + numberOfReadLog + 1);
            return new MerkleTree(merkleTreeRes.merkleTreeLeft, merkleTreeRightCurrent);
        }

        if(logs.size() == 1) {
            return appendSingleByRecursivity(logs.poll(), m, rank);
        }

        return new MerkleTree(m.merkleTreeLeft, appendManyByRecursivity(logs, m.merkleTreeLeft, rank));
    }
}
