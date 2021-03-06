import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

/**
 * Created by mashiru on 2/11/18.
 */
public class SkGroup { // implements Comparable{
    protected List<SkNode> gNodes;
    protected List<SkNode> dominatedNodes;
    protected long maxSizeOfDominatedGroups;
    protected List<SkGroup> dominatedGroups;
    protected long sizeOfDominatedGroups;
    protected String dominateType;
    protected int updateThreshold;
    protected int processThreshold;

    public SkGroup(String type) {
        dominateType = type;
        assert(dominateType == "GP" || dominateType == "GG");
        gNodes = new ArrayList<>();
        dominatedNodes = new ArrayList<>();
        maxSizeOfDominatedGroups = 0;
        dominatedGroups = new ArrayList<>();
        sizeOfDominatedGroups = 0;
    }

    public SkGroup(String type, SkNode gnode) {
        dominateType = type;
        assert(dominateType == "GP" || dominateType == "GG");
        gNodes = new ArrayList<>();
        dominatedNodes = new ArrayList<>();
        maxSizeOfDominatedGroups = 0;
        dominatedGroups = new ArrayList<>();
        sizeOfDominatedGroups = 0;
        addGroupNode(gnode);
    }

    public SkGroup(SkGroup another) {
        this.dominateType = another.getDominateType();
        this.gNodes = new ArrayList<>(another.getGroupNodes());
        this.dominatedNodes = new ArrayList<>(another.getDominatedNodes());
        this.maxSizeOfDominatedGroups = another.getMaxSizeOfDominatedGroups();
        this.dominatedGroups = new ArrayList<>(another.getDominatedGroups());
        this.sizeOfDominatedGroups = another.getSizeOfDominatedGroups();
    }

    public SkGroup(String type, List<SkNode> gnodes) {
        dominateType = type;
        assert(dominateType == "GP" || dominateType == "GG");
        gNodes = new ArrayList<>();
        dominatedNodes = new ArrayList<>();
        maxSizeOfDominatedGroups = 0;
        dominatedGroups = new ArrayList<>();
        sizeOfDominatedGroups = 0;
        addGroupNodes(gnodes);
    }

    // before invoking this construct, MAKE SURE that the kids is the merged result for dominatedNodes of gnodes
    public SkGroup(String type, List<SkNode> gnodes, List<SkNode> dnodes) {
        dominateType = type;
        assert(dominateType == "GP" || dominateType == "GG");
        gNodes = gnodes;
        dominatedNodes = dnodes;
        maxSizeOfDominatedGroups = 0;
        dominatedGroups = new ArrayList<>();
        sizeOfDominatedGroups = 0;
    }

    public String getDominateType() { return dominateType; }

    public void addGroupNodes(List<SkNode> gnodes) {
        gNodes = merge(gNodes, gnodes); // use merge rather than addAll
        if (dominateType == "GP")
            for (SkNode gnode: gnodes)
                updateChildrenAndDominates(gnode);
        else {
            maxSizeOfDominatedGroups += 1;
            for (SkNode gnode : gnodes)
                maxSizeOfDominatedGroups *= (gnode.getSizeOfDominatedNodes() + 1);
            maxSizeOfDominatedGroups -= 1;
        }
    }

    public void addGroupNode(SkNode gnode) {
        /*List<SkNode> gnodes = new ArrayList<>();
        gnodes.add(gnode);
        addGroupNodes(gnodes);*/
        gNodes.add(gnode);
        if (dominateType == "GP")
            updateChildrenAndDominates(gnode);
        else {
            maxSizeOfDominatedGroups += 1;
            maxSizeOfDominatedGroups *= (gnode.getSizeOfDominatedNodes() + 1);
            maxSizeOfDominatedGroups -= 1;
        }
    }

    protected void updateChildrenAndDominates(SkNode node) {
        dominatedNodes  = merge(dominatedNodes, node.getChildren());
    }

    public List<SkNode> getGroupNodes() { return gNodes; }

    public List<SkNode> getDominatedNodes() { return dominatedNodes; }

    public long getMaxSizeOfDominatedGroups() { return maxSizeOfDominatedGroups; }

    public long getSizeOfDominatedGroups() { return sizeOfDominatedGroups; }

    public List<SkGroup> getDominatedGroups() { return dominatedGroups; }

    public int getSizeOfDominatedNodes() { return dominatedNodes.size(); }

    public int getGroupSize() { return gNodes.size(); }

    protected List<SkGroup> updateDominateInfo(List<SkGroup> currDominatedGroups){
        sizeOfDominatedGroups += currDominatedGroups.size();
        currDominatedGroups = null; // clear all dominatedGroups
        currDominatedGroups = new ArrayList<>();
        return currDominatedGroups;
    }

    protected List<List<SkNode>> twoClassifyMerge(List<SkNode> a, List<SkNode> b) {
        List<List<SkNode>> result = new ArrayList<>();
        List<SkNode> pureA = new ArrayList<>();
        List<SkNode> pureB = new ArrayList<>();
        List<SkNode> share = new ArrayList<>();
        int aIdx = 0; int bIdx = 0;
        SkNode aEle, bEle;
        while (aIdx < a.size() && bIdx < b.size()) {
            if ((aEle=a.get(aIdx)).id < (bEle=b.get(bIdx)).id) {
                pureA.add(aEle);
                aIdx++;
            } else if (aEle.id > bEle.id) {
                pureB.add(bEle);
                bIdx++;
            } else {
                share.add(aEle);
                aIdx++; bIdx++;
            }
        }
        if (aIdx != a.size())
            pureA.addAll(a.subList(aIdx, a.size()));
        if (bIdx != b.size())
            pureB.addAll(b.subList(bIdx, b.size()));

        result.add(pureA);
        result.add(pureB);
        result.add(share);
        return result;
    }

    protected List<List<SkNode>> threeClassifyMerge(List<SkNode> a, List<SkNode> b, List<SkNode> c) {
        List<List<SkNode>> a_bResult = twoClassifyMerge(a, b); // pureA, pureB, shareAB
        List<SkNode> pureA = a_bResult.get(0); List<SkNode> pureB = a_bResult.get(1); List<SkNode> AB = a_bResult.get(2);

        List<List<SkNode>> a_cResult = twoClassifyMerge(pureA, c); // pureA(final), pureC, shareAC(final)
        pureA = a_cResult.get(0); List<SkNode> pureC = a_cResult.get(1); List<SkNode> AC = a_cResult.get(2);

        List<List<SkNode>> b_cResult = twoClassifyMerge(pureB, pureC); // pureB(final), pureC, shareBC(final)
        pureB = b_cResult.get(0); pureC = b_cResult.get(1); List<SkNode> BC = b_cResult.get(2);

        List<List<SkNode>> ab_cResult = twoClassifyMerge(AB, pureC); // ab(final), pureC(final), shareABC(final)
        AB = ab_cResult.get(0); pureC = ab_cResult.get(1); List<SkNode> ABC = ab_cResult.get(2);

        List<List<SkNode>> result = new ArrayList<>();
        result.add(pureA); result.add(pureB); result.add(pureC);
        result.add(AB); result.add(AC); result.add(BC); result.add(ABC);

        return result;
    }

    private long factorial(long start, long end) {
        long result = 1;
        for(long ele = start; ele<=end; ele++)
            result *= ele;
        return result;
    }

    public long combCalculateDominatedGroups(List<List<SkNode>> groupTrees4Check) {
        long combTotal;
        if (groupTrees4Check.size() == 3) {
            List<List<SkNode>> result = threeClassifyMerge(groupTrees4Check.get(0), groupTrees4Check.get(1), groupTrees4Check.get(2));
            int a = result.get(0).size();
            int b = result.get(1).size();
            int c = result.get(2).size();
            int d = result.get(3).size();
            int e = result.get(4).size();
            int f = result.get(5).size();
            int g = result.get(6).size();
            int total = a + b + c + d + e + f + g;
            int noa = b + c + d + e + f + g;
            int nob = a + c + d + e + f + g;
            int noc = a + b + d + e + f + g;
            combTotal = factorial(total - 2, total) / 6;
            combTotal -= factorial(a - 1, a) / 2 * noa - factorial(b - 1, b) / 2 * nob - factorial(c - 1, c) / 2 * noc;
            combTotal -= factorial(d - 1, b) / 2 * (a + b) - factorial(e - 1, e) / 2 * (a + c) - factorial(f - 1, f) / 2 * (b + c);
            combTotal -= factorial(a - 2, a) / 6 - factorial(b - 2, b) / 6 - factorial(c - 2, c) / 6 - factorial(d - 2, d) / 6 - factorial(e - 2, e) / 6 - factorial(f - 2, f) / 6;
        } else {
            List<List<SkNode>> result = twoClassifyMerge(groupTrees4Check.get(0), groupTrees4Check.get(1));
            int a = result.get(0).size();
            int b = result.get(1).size();
            int c = result.get(2).size();
            combTotal = factorial(a+b+c-1, a+b+c)/2 - factorial(a-1, a)/2-factorial(b-1, b)/2;
        }

        return combTotal;
    }

    protected List<SkGroup> uniqueFilter(List<SkGroup> groups) {
        // System.out.println("Unique filtering...");
        Set<SkGroup> groupsSet = new HashSet<>();
        if (groups.size() < updateThreshold) {
            groupsSet.addAll(groups);
            groups.clear();
            groups.addAll(groupsSet);
        } else {
            int length = groups.size() % updateThreshold + 1;
            int startIdx = groups.size() - length;
            List<SkGroup> newGroups = new ArrayList<>(groups.subList(0, startIdx));
            groupsSet.addAll(groups.subList(startIdx, groups.size()));
            groups.clear();
            newGroups.addAll(groupsSet);
            groups = newGroups;
        }
        // System.out.println("Unique filtering done");
        return groups;
    }

    protected List<SkGroup> uniqueFilterByBatch(List<SkGroup> groups) {
        if (groups.size() < processThreshold) {
            Set<SkGroup> groupsSet = new HashSet<>();
            groupsSet.addAll(groups);
            groups.clear();
            groups.addAll(groupsSet);
        } else {
            // System.out.println("Group-Group uniqueFilterByBatch, threshold " + processThreshold);
            List<SkGroup> newGroups = new ArrayList<>();
            while (groups.size() != 0) {
                int processSize = groups.size() > processThreshold ? processThreshold : groups.size();
                Set<SkGroup> groupsSet = new HashSet<>();
                groupsSet.addAll(groups.subList(0, processSize));
                groups.removeAll(groups.subList(0, processSize));
                newGroups.addAll(groupsSet);
                groupsSet = null;
            }
            groups = newGroups;
        }
        // System.out.println("Unique filtering done");
        return groups;
    }

    public void calculateDominatedGroups(boolean stepwise) {
        List<List<SkNode>> groupTrees4Check = new ArrayList<>();
        for (SkNode gnode : gNodes) {
            List<SkNode> nodes4Check = new ArrayList<>();
            nodes4Check.add(gnode); // add this group node into the check list
            // add the children in the first (endLayerIdx+1) layers or in the first (percent) percentage of this group node into the check list
            nodes4Check.addAll(gnode.getChildren());
            groupTrees4Check.add(nodes4Check);
        }

        Collections.sort(groupTrees4Check, new Comparator<List<SkNode>>() {
            @Override
            public int compare(List<SkNode> list1, List<SkNode> list2) {
                return list2.size() - list1.size();
            }
        });

        if (getGroupSize() <= 3) { // Using combination method
            sizeOfDominatedGroups = combCalculateDominatedGroups(groupTrees4Check);
        } else {
            if (stepwise) processThreshold = 1000000;
            dominatedGroups = calculateDominatedGroups(groupTrees4Check, new SkGroup("GG"), stepwise); // update dominatedGroups
        /*Set<SkGroup> dominatedGroupsSet = new HashSet<SkGroup>(dominatedGroups);
        dominatedGroups = new ArrayList<>(dominatedGroupsSet);*/
            if (!stepwise)
                dominatedGroups = uniqueFilter(dominatedGroups);
            else
                dominatedGroups = uniqueFilterByBatch(dominatedGroups);
            dominatedGroups = updateDominateInfo(dominatedGroups);
        }
    }

    protected List<SkGroup>  calculateDominatedGroups(List<List<SkNode>> groupTrees4Check, SkGroup dominatedGroup, boolean stepwise) {
        List<SkGroup> currDominatedGroups = new ArrayList<>();
        if (stepwise == false) searchDominatedGroups(groupTrees4Check, dominatedGroup, getGroupSize(), currDominatedGroups);
        else searchDominatedGroupsStepwise(groupTrees4Check, dominatedGroup, currDominatedGroups);
        return currDominatedGroups;
    }

    protected List<SkGroup> searchDominatedGroupsStepwise(List<List<SkNode>> groupTrees4Check, SkGroup dominatedGroup, List<SkGroup> currDominatedGroups) {
        // first step: get the dominateGroups with group size of 2
        searchDominatedGroups(groupTrees4Check.subList(0, 2), dominatedGroup, 2, currDominatedGroups, true); //
        currDominatedGroups = uniqueFilterByBatch(currDominatedGroups);
        System.out.println("Group-Group searchDominatedGroupsStepwise of size 2: " + (currDominatedGroups.size()));
        for (int gTIdx = 2; gTIdx<groupTrees4Check.size(); gTIdx++) { // for each tree
            List<SkNode> currGroupTree = groupTrees4Check.get(gTIdx);
            List<SkGroup> newCurrDominatedGroups = new ArrayList<>(); // new dominatedGroups contains group nodes with size of one more than group in currDominatedGroups
            for (SkGroup dGroup: currDominatedGroups) { // for each dominatedGroup
                for (SkNode currNode: currGroupTree) { // for each node in curr tree
                    if (!dGroup.getGroupNodes().contains(currNode)) { // if curr dominatedGroup not contains curr node
                        SkGroup newDGroup = new SkGroup(dGroup); // create a new dominated group with curr dominatedGroup and the curr node
                        newDGroup.addGroupNode(currNode);
                        newCurrDominatedGroups.add(newDGroup); // add the new dominatedGroup into the new dominatedGroups
                    }
                }
                if (newCurrDominatedGroups.size() % 100000 < 80000)
                    System.out.println("Update new dominated groups: " + newCurrDominatedGroups.size());
                if (newCurrDominatedGroups.size() >= processThreshold)
                    newCurrDominatedGroups = uniqueFilterByBatch(newCurrDominatedGroups);
            }
            currDominatedGroups = null;
            currDominatedGroups = uniqueFilterByBatch(newCurrDominatedGroups); // update currDominatedGroups
            newCurrDominatedGroups = null;
            System.out.println("Group-Group searchDominatedGroupsStepwise of size " + (gTIdx+1) + ": "+ (currDominatedGroups.size()));
        }
        return currDominatedGroups;
    }

    protected void searchDominatedGroups(List<List<SkNode>> groupTrees4Check, SkGroup dominatedGroup, int targetSize, List<SkGroup> currDominatedGroups) {
        searchDominatedGroups(groupTrees4Check, dominatedGroup, targetSize, currDominatedGroups, false);
    }

    protected void searchDominatedGroups(List<List<SkNode>> groupTrees4Check, SkGroup dominatedGroup,
                                         int targetSize, List<SkGroup> currDominatedGroups, boolean stepwise) {
        if (dominatedGroup.getGroupSize() == targetSize) { // if the dominated group has the same size as this group, then stop
            Collections.sort(dominatedGroup.getGroupNodes(), new Comparator<SkNode>() {
                @Override
                public int compare(SkNode node1, SkNode node2) {
                    return node1.getId() - node2.getId();
                }
            });
            /*if (!dominatedGroups.contains(dominatedGroup)) // if not contains
                dominatedGroups.add(dominatedGroup);*/
            currDominatedGroups.add(dominatedGroup);
            updateThreshold = 1000000;//10000000;
            if (currDominatedGroups.size() % updateThreshold == updateThreshold-1) {
                //System.out.println("Size of the Current dominated groups: " + dominatedGroups.size());
                //System.out.println("Size of the Total dominated groups before filtering: " + sizeOfDominatedGroups);

                if (!stepwise) { // if not stepwise = approx is true, the sizeOfDominatedGroups will be updated and currDominatedGroups will be emptied
                    currDominatedGroups = uniqueFilter(currDominatedGroups);
                    currDominatedGroups = updateDominateInfo(currDominatedGroups);
                } else
                    currDominatedGroups = uniqueFilterByBatch(currDominatedGroups);
                //System.out.println("Size of the Total dominated groups after filtering: " + sizeOfDominatedGroups);
            }
            return;
        }

        List<SkNode> nodes4Check = groupTrees4Check.get(0);
        for (SkNode node: nodes4Check) {
            if (!dominatedGroup.getGroupNodes().contains(node)) { // if current node is not contained in the dominated group, then add it
                SkGroup newDominatedGroup = new SkGroup(dominatedGroup);
                newDominatedGroup.addGroupNode(node);
                searchDominatedGroups(groupTrees4Check.subList(1, groupTrees4Check.size()), newDominatedGroup, targetSize, currDominatedGroups, stepwise);
            }
        }
    }

    // Merge two group of points
    public List<SkNode> merge(List<SkNode> a, List<SkNode> b) {
        if (a.size() == 0) return b;
        if (b.size() == 0) return a;
        List<SkNode> result = new ArrayList<SkNode>();
        int aIdx = 0; int bIdx = 0;
        SkNode aEle, bEle;
        while (aIdx < a.size() && bIdx < b.size()) {
            if ((aEle=a.get(aIdx)).id < (bEle=b.get(bIdx)).id) {
                result.add(aEle);
                aIdx++;
            } else if (aEle.id > bEle.id) {
                result.add(bEle);
                bIdx++;
            } else {
                result.add(aEle);
                aIdx++; bIdx++;
            }
        }
        // append the rest
        result.addAll(new ArrayList<SkNode>(aIdx == a.size() ? b.subList(bIdx, b.size()) : a.subList(aIdx, a.size())));

        return result;
    }

    public void print() {
        System.out.println("\n==========Group size: "+ getGroupSize() + " Number of dominatedNodes: " + getSizeOfDominatedNodes() +"==========");
        System.out.println("Group node info:");
        for (SkNode node: gNodes)
            node.print();
    }

    public void printGroups() {
        System.out.println("\n==========Group size: "+ getGroupSize() + " Number of dominatedGroups: " + getSizeOfDominatedGroups() +"==========");
        System.out.println("Group info:");
        for (SkNode node: gNodes)
            node.print();
    }

    /*@Override
    getGroupDominatedNodes*/

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!SkGroup.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final SkGroup other = (SkGroup) obj;

        if (!this.getGroupNodes().equals(other.getGroupNodes())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (SkNode node: gNodes)
            hash += node.hashCode();
        return hash;
    }
}
