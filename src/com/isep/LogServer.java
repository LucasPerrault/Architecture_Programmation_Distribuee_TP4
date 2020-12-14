package com.isep;

import java.io.BufferedReader;
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
            BufferedReader reader = new BufferedReader(new FileReader(inputTextFile));
            String line = reader.readLine();
            int rangeInputIndex = 0;
            while (line != null) {
                merkleQueue.add(new MerkleTree(line, rangeInputIndex));
                line = reader.readLine();
                rangeInputIndex++;
            }
            reader.close();
            buildTree(merkleQueue);
        } catch(FileNotFoundException e) {
            System.out.println("FileNotFoundException. No such file found.");
            System.exit(1);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public MerkleTree getMerkleTree() {
        return merkleTree;
    }

    public HashSHA256 currentRootHashSHA256() {
        return this.merkleTree.getHashSHA256();
    }

    public LinkedList<HashSHA256> genPath(int rangeIndex) {
        return genPathRecursive(rangeIndex, merkleTree, new LinkedList<HashSHA256>());
    }

    public LinkedList<HashSHA256> genProof(int rangeIndex){
        return genProofRecursive(rangeIndex, merkleTree, new LinkedList<HashSHA256>());
    }

    public void append(String log) {
        merkleTree = appendSingleByRecursivity(log, merkleTree, merkleTree.getSize());
    }

    public void append(LinkedList<String> list) {
        merkleTree = appendManyByRecursivity(list, merkleTree, merkleTree.getSize());
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
        boolean hasSameIndex = m.getStartRangeLog() == m.getEndRangeLog() && m.getEndRangeLog() == rangeIndex;
        if (hasSameIndex) {
            return path;
        }

        boolean hasTreeLeftSuperiorEnd = m.getMerkleTreeLeft() != null && m.getMerkleTreeLeft().getEndRangeLog() >= rangeIndex;
        if (hasTreeLeftSuperiorEnd) {
            path.addFirst(m.getMerkleTreeRight().getHashSHA256());
            return genPathRecursive(rangeIndex, m.getMerkleTreeLeft(), path);
        }

        boolean hasTreeRightSuperiorEnd = m.getMerkleTreeRight() != null && m.getMerkleTreeRight().getEndRangeLog() >= rangeIndex;
        if (hasTreeRightSuperiorEnd) {
            path.addFirst(Objects.requireNonNull(m.getMerkleTreeLeft()).getHashSHA256());
            return genPathRecursive(rangeIndex, m.getMerkleTreeRight(), path);
        }

        return path;
    }

    /* Make the proof with recursivity */
    private LinkedList<HashSHA256> genProofRecursive(int rangeIndex, MerkleTree m, LinkedList<HashSHA256> proof) {
        if(m.getEndRangeLog() < rangeIndex || m.getStartRangeLog() > rangeIndex) {
            return proof;
        }

        if (m.getEndRangeLog() == rangeIndex){
            proof.addFirst(m.getHashSHA256());
            return proof;
        }

        if (m.getMerkleTreeLeft() != null && m.getMerkleTreeLeft().getEndRangeLog() < rangeIndex) {
            proof.addFirst(m.getMerkleTreeLeft().getHashSHA256());
            return genProofRecursive(rangeIndex, m.getMerkleTreeRight(), proof);
        }

        if (m.getMerkleTreeLeft() != null) {
            proof.addFirst(m.getMerkleTreeRight().getHashSHA256());
            proof = genProofRecursive(rangeIndex, m.getMerkleTreeLeft(), proof);
            return proof;
        }

        return null;
    }

    /* Append single by recursivity */
    private MerkleTree appendSingleByRecursivity(String log, MerkleTree m, int rank) {
        if(m == null) {
            return new MerkleTree(log, 0);
        }

        if(m.getSize() == m.getNext()) {
            MerkleTree newElement = new MerkleTree(log, rank);
            return new MerkleTree(m, newElement);
        }

        MerkleTree merkleTreeRightCurrent = appendSingleByRecursivity(log, m.getMerkleTreeRight(), rank);
        return new MerkleTree(m.getMerkleTreeLeft(), merkleTreeRightCurrent);
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

        int numberOfReadLog = m.getNext() - m.getSize();
        MerkleTree merkleTreeRes;
        if(numberOfReadLog == 0){
            merkleTreeRes = new MerkleTree(m, new MerkleTree(logs.poll(), rank));
            return appendManyByRecursivity(logs, merkleTreeRes, rank + 1);
        }

        if(logs.size() > numberOfReadLog){
            LinkedList<String> elements = logs;
            LinkedList<String> completeTreeElements = new LinkedList<String>();

            for(int i = 0; i < numberOfReadLog; i++) completeTreeElements.add(elements.poll());

            merkleTreeRes = new MerkleTree(m.getMerkleTreeLeft(), appendManyByRecursivity(completeTreeElements, m.getMerkleTreeRight(), rank));
            merkleTreeRes = new MerkleTree(merkleTreeRes, new MerkleTree(elements.poll(), rank + numberOfReadLog));
            MerkleTree merkleTreeRightCurrent = appendManyByRecursivity(elements, merkleTreeRes.getMerkleTreeRight(), rank + numberOfReadLog + 1);
            return new MerkleTree(merkleTreeRes.getMerkleTreeLeft(), merkleTreeRightCurrent);
        }

        if(logs.size() == 1) {
            return appendSingleByRecursivity(logs.poll(), m, rank);
        }

        return new MerkleTree(m.getMerkleTreeLeft(), appendManyByRecursivity(logs, m.getMerkleTreeLeft(), rank));
    }
}
