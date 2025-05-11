package ruanjian1;

import java.util.*;
import java.io.*;

public class TextGraph {
    private DirectedGraph graph;

    public static void main(String[] args) {
        TextGraph app = new TextGraph();
        app.run(args);
    }

    private void run(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filePath = getFilePath(args, scanner);
        if (filePath == null) {
            System.out.println("No file provided. Exiting.");
            return;
        }
        List<String> words = preprocessFile(filePath);
        if (words.isEmpty()) {
            System.out.println("No words processed. Exiting.");
            return;
        }
        buildGraph(words);

        while (true) {
            printMenu();
            int choice = getChoice(scanner);
            scanner.nextLine(); // Consume newline
            switch (choice) {
                case 1:
                    showDirectedGraph();
                    break;
                case 2:
                    handleQueryBridgeWords(scanner);
                    break;
                case 3:
                    handleGenerateNewText(scanner);
                    break;
                case 4:
                    handleCalcShortestPath(scanner);
                    break;
                case 5:
                    handleCalcPageRank(scanner);
                    break;
                case 6:
                    handleRandomWalk();
                    break;
                case 7:
                    System.out.println("Exiting.");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private String getFilePath(String[] args, Scanner scanner) {
        if (args.length > 0) {
            return args[0];
        } else {
            System.out.print("Enter file path: ");
            return scanner.nextLine();
        }
    }

    private List<String> preprocessFile(String filePath) {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("[^a-zA-Z]", " ");
                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    if (!token.isEmpty()) {
                        words.add(token.toLowerCase());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return words;
    }

    private void buildGraph(List<String> words) {
        graph = new DirectedGraph();
        for (int i = 0; i < words.size() - 1; i++) {
            String src = words.get(i);
            String dest = words.get(i + 1);
            graph.addEdge(src, dest);
        }
    }

    private void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Show directed graph");
        System.out.println("2. Query bridge words");
        System.out.println("3. Generate new text");
        System.out.println("4. Calculate shortest path");
        System.out.println("5. Calculate PageRank");
        System.out.println("6. Random walk");
        System.out.println("7. Exit");
        System.out.print("Enter your choice: ");
    }

    private int getChoice(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.print("Invalid input. Enter a number: ");
        }
        return scanner.nextInt();
    }

    public void showDirectedGraph() {
        System.out.println("\nDirected Graph:");
        for (String node : graph.getNodes()) {
            System.out.print(node + " -> ");
            Map<String, Integer> edges = graph.getEdges(node);
            if (edges.isEmpty()) {
                System.out.println();
                continue;
            }
            List<String> edgeDescs = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : edges.entrySet()) {
                edgeDescs.add(entry.getKey() + "(" + entry.getValue() + ")");
            }
            System.out.println(String.join(", ", edgeDescs));
        }
    }

    private void handleQueryBridgeWords(Scanner scanner) {
        System.out.print("Enter two words (separated by space): ");
        String[] input = scanner.nextLine().split("\\s+");
        if (input.length != 2) {
            System.out.println("Invalid input. Need two words.");
            return;
        }
        String result = queryBridgeWords(input[0], input[1]);
        System.out.println(result);
    }

    public String queryBridgeWords(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        if (!graph.containsNode(word1) || !graph.containsNode(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        List<String> bridges = new ArrayList<>();
        Map<String, Integer> word1Edges = graph.getEdges(word1);
        for (String candidate : word1Edges.keySet()) {
            Map<String, Integer> candidateEdges = graph.getEdges(candidate);
            if (candidateEdges.containsKey(word2)) {
                bridges.add(candidate);
            }
        }

        if (bridges.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        }

        return formatBridgeWordsOutput(bridges, word1, word2);
    }

    private String formatBridgeWordsOutput(List<String> bridges, String word1, String word2) {
        if (bridges.size() == 1) {
            return "The bridge words from " + word1 + " to " + word2 + " are: " + bridges.get(0) + ".";
        } else {
            String joined = String.join(", ", bridges.subList(0, bridges.size() - 1))
                    + " and " + bridges.get(bridges.size() - 1);
            return "The bridge words from " + word1 + " to " + word2 + " are: " + joined + ".";
        }
    }

    private void handleGenerateNewText(Scanner scanner) {
        System.out.print("Enter text: ");
        String inputText = scanner.nextLine();
        String result = generateNewText(inputText);
        System.out.println("Generated text: " + result);
    }

    public String generateNewText(String inputText) {
        String[] inputWords = inputText.split("\\s+");
        List<String> newText = new ArrayList<>();
        if (inputWords.length < 1) return inputText;

        newText.add(inputWords[0]);
        for (int i = 0; i < inputWords.length - 1; i++) {
            String word1 = inputWords[i].toLowerCase();
            String word2 = inputWords[i + 1].toLowerCase();
            List<String> bridges = findBridgeWords(word1, word2);
            if (!bridges.isEmpty()) {
                String bridge = bridges.get(new Random().nextInt(bridges.size()));
                newText.add(bridge);
            }
            newText.add(inputWords[i + 1]);
        }
        return String.join(" ", newText);
    }

    private List<String> findBridgeWords(String word1, String word2) {
        List<String> bridges = new ArrayList<>();
        if (!graph.containsNode(word1) || !graph.containsNode(word2)) return bridges;

        Map<String, Integer> edges = graph.getEdges(word1);
        for (String candidate : edges.keySet()) {
            if (graph.getEdges(candidate).containsKey(word2)) {
                bridges.add(candidate);
            }
        }
        return bridges;
    }

    private void handleCalcShortestPath(Scanner scanner) {
        System.out.print("Enter two words (separated by space): ");
        String[] input = scanner.nextLine().split("\\s+");
        if (input.length != 2) {
            System.out.println("Invalid input. Need two words.");
            return;
        }
        String result = calcShortestPath(input[0], input[1]);
        System.out.println(result);
    }

    public String calcShortestPath(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        if (!graph.containsNode(word1) || !graph.containsNode(word2)) {
            return "Either " + word1 + " or " + word2 + " not in graph.";
        }

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(nd -> nd.distance));

        for (String node : graph.getNodes()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(word1, 0);
        pq.add(new NodeDistance(word1, 0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            if (current.distance > dist.get(current.node)) continue;

            Map<String, Integer> neighbors = graph.getEdges(current.node);
            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                String v = neighbor.getKey();
                int weight = neighbor.getValue();
                int alt = current.distance + weight;
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, current.node);
                    pq.add(new NodeDistance(v, alt));
                }
            }
        }

        if (dist.get(word2) == Integer.MAX_VALUE) {
            return "No path from " + word1 + " to " + word2 + ".";
        }

        List<String> path = reconstructPath(prev, word2);
        return formatPath(path, dist.get(word2));
    }

    private List<String> reconstructPath(Map<String, String> prev, String target) {
        LinkedList<String> path = new LinkedList<>();
        String current = target;
        while (current != null) {
            path.addFirst(current);
            current = prev.get(current);
        }
        return path;
    }

    private String formatPath(List<String> path, int length) {
        if (path.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("Path: ");
        sb.append(String.join(" → ", path));
        sb.append(" (Length: ").append(length).append(")");
        return sb.toString();
    }

    private void handleCalcPageRank(Scanner scanner) {
        System.out.print("Enter a word: ");
        String word = scanner.nextLine().trim().toLowerCase();
        double pr = calcPageRank(word);
        System.out.printf("PageRank of %s: %.4f\n", word, pr);
    }

    public double calcPageRank(String word) {
        if (!graph.containsNode(word)) return -1.0;
        Map<String, Double> pr = computePageRank();
        return pr.getOrDefault(word, 0.0);
    }

    private Map<String, Double> computePageRank() {
        final double damping = 0.85;
        final int maxIter = 100;
        final double epsilon = 0.0001;

        Set<String> nodes = graph.getNodes();
        int n = nodes.size();
        Map<String, Double> pr = new HashMap<>();
        double initial = 1.0 / n;
        
        // 使用传统循环初始化PR值
        for (String node : nodes) {
            pr.put(node, initial);
        }

        // 构建入边索引
        Map<String, List<String>> inEdges = new HashMap<>();
        for (String node : nodes) {
            inEdges.put(node, new ArrayList<>());
        }
        for (String u : nodes) {
            for (String v : graph.getEdges(u).keySet()) {
                inEdges.get(v).add(u);
            }
        }

        // PageRank迭代计算
        for (int iter = 0; iter < maxIter; iter++) {
            Map<String, Double> newPr = new HashMap<>();
            double danglingSum = 0.0;
            
            // 计算悬挂节点贡献
            for (String u : nodes) {
                if (graph.getEdges(u).isEmpty()) {
                    danglingSum += pr.get(u);
                }
            }

            // 更新每个节点的PR值
            for (String v : nodes) {
                double sum = 0.0;
                for (String u : inEdges.get(v)) {
                    int outDegree = graph.getEdges(u).size();
                    if (outDegree > 0) {
                        sum += pr.get(u) / outDegree;
                    }
                }
                sum = damping * sum 
                    + damping * danglingSum / n 
                    + (1 - damping) / n;
                newPr.put(v, sum);
            }

            // 检查收敛条件
            boolean converged = true;
            for (String node : nodes) {
                if (Math.abs(newPr.get(node) - pr.get(node)) > epsilon) {
                    converged = false;
                    break;
                }
            }
            pr = newPr;
            if (converged) break;
        }

        return pr;
    }

    private void handleRandomWalk() {
        String result = randomWalk();
        System.out.println("Random walk: " + result);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("random_walk.txt"))) {
            writer.write(result);
        } catch (IOException e) {
            System.out.println("Error writing random walk result.");
        }
    }

    public String randomWalk() {
        List<String> path = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>();
        List<String> nodes = new ArrayList<>(graph.getNodes());
        if (nodes.isEmpty()) return "";

        Random rand = new Random();
        String current = nodes.get(rand.nextInt(nodes.size()));
        path.add(current);

        while (true) {
            Map<String, Integer> edges = graph.getEdges(current);
            if (edges.isEmpty()) break;

            List<String> candidates = new ArrayList<>();
            List<Integer> weights = new ArrayList<>();
            int totalWeight = edges.values().stream().mapToInt(i -> i).sum();
            if (totalWeight == 0) break;

            edges.forEach((k, v) -> {
                candidates.add(k);
                weights.add(v);
            });

            int r = rand.nextInt(totalWeight);
            int sum = 0;
            String next = null;
            for (int i = 0; i < weights.size(); i++) {
                sum += weights.get(i);
                if (r < sum) {
                    next = candidates.get(i);
                    break;
                }
            }

            String edge = current + "->" + next;
            if (visitedEdges.contains(edge)) break;
            visitedEdges.add(edge);

            path.add(next);
            current = next;
        }

        return String.join(" ", path);
    }

    static class DirectedGraph {
        private final Map<String, Map<String, Integer>> adjList = new HashMap<>();
        private final Set<String> nodes = new HashSet<>();

        public void addEdge(String src, String dest) {
            src = src.toLowerCase();
            dest = dest.toLowerCase();
            nodes.add(src);
            nodes.add(dest);
            adjList.computeIfAbsent(src, k -> new HashMap<>())
                    .merge(dest, 1, Integer::sum);
        }

        public boolean containsNode(String word) {
            return nodes.contains(word.toLowerCase());
        }

        public Map<String, Integer> getEdges(String node) {
            return adjList.getOrDefault(node.toLowerCase(), Collections.emptyMap());
        }

        public Set<String> getNodes() {
            return new HashSet<>(nodes);
        }
    }

    static class NodeDistance {
        String node;
        int distance;

        public NodeDistance(String node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }
}
