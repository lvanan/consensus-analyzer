package utils;

import exceptions.GraphException;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class DrawingUtils extends JFrame {
    public void drawGraph (String jsonPath, String graphPath) throws IOException, GraphException {
        File consensusResult = new File(jsonPath);
        File graphFile = new File(graphPath);

        BufferedReader lineReader = new BufferedReader(new FileReader(consensusResult));
        String lineText;

        List<Double> probabilities = new ArrayList<>();
        List<Double> expectedMessages = new ArrayList<>();

        while ((lineText = lineReader.readLine()) != null) {
            for (int i = 0; i < lineText.length(); ++i) {
                if (lineText.charAt(i) == 'p') {
                    if (lineText.startsWith("probability", i)) {
                        i = 14 + i;
                        StringBuilder prob = new StringBuilder();

                        while (lineText.charAt(i) != ',') {
                            prob.append(lineText.charAt(i));
                            ++i;
                        }

                        double probability = Double.parseDouble(prob.toString());

                        if (probability < 0) {
                            throw new GraphException("Probability less than 0");
                        } else if (probability > 1) {
                            throw new GraphException("Probability greater than 1");
                        }

                        probabilities.add(Double.parseDouble(prob.toString()));
                    }
                } else if (lineText.charAt(i) == 'e') {
                    if (lineText.startsWith("expectedMessages", i)) {
                        i = 19 + i;
                        StringBuilder expectedMessage = new StringBuilder();

                        while (lineText.charAt(i) != ',') {
                            expectedMessage.append(lineText.charAt(i));
                            ++i;
                        }

                        double expMessage = Double.parseDouble(expectedMessage.toString());

                        if (expMessage < 0) {
                            throw new GraphException("Expected Message less than 0");
                        }

                        expectedMessages.add(expMessage);
                    }
                }
            }
        }

        if (probabilities.size() != expectedMessages.size()) {
            throw new GraphException("Size of Probabilities list differs from size of ExpectedMessages list");
        }

        GraphPanel graph = new GraphPanel(probabilities, expectedMessages, graphFile);
        graph.run();
    }

    private static class GraphPanel extends JPanel {
        private final Color lineColor = new Color(16, 162, 107, 180);
        private final Color pointColor = new Color(0, 0, 0, 180);
        private final Color gridColor = new Color(200, 200, 200, 200);
        private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
        private List<Double> xCoordinates;
        private List<Double> yCoordinates;
        private final File graphFile;

        public GraphPanel(List<Double> xCoordinates, List<Double> yCoordinates, File graphFile) {
            this.xCoordinates = xCoordinates;
            this.yCoordinates = yCoordinates;
            this.graphFile = graphFile;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padding = 25;
            int labelPadding = 25;
            double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (xCoordinates.size() - 1);
            double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (getMaxScore() - getMinScore());

            List<Point> graphPoints = new ArrayList<>();
            for (int i = 0; i < xCoordinates.size(); i++) {
                int x1 = (int) (i * xScale + padding + labelPadding);
                int y1 = (int) ((getMaxScore() - yCoordinates.get(i)) * yScale + padding);
                graphPoints.add(new Point(x1, y1));
            }

            // draw white background
            graphics.setColor(Color.WHITE);
            graphics.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
            graphics.setColor(Color.BLACK);

            // create hatch marks and grid lines for y-axis.
            int pointWidth = 6;
            int pointHeight = 6;
            int numberYDivisions = 10;

            for (int i = 0; i < numberYDivisions + 1; i++) {
                int x0 = padding + labelPadding;
                int x1 = pointWidth + padding + labelPadding;
                int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);

                if (xCoordinates.size() > 0) {
                    graphics.setColor(gridColor);
                    graphics.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y0);
                    graphics.setColor(Color.BLACK);
                    String yLabel = ((int) ((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
                    FontMetrics metrics = graphics.getFontMetrics();
                    int labelWidth = metrics.stringWidth(yLabel);
                    graphics.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
                }

                graphics.drawLine(x0, y0, x1, y0);
            }

            // and for x-axis
            for (int i = 0; i < xCoordinates.size(); i++) {
                if (xCoordinates.size() > 1) {
                    int x0 = i * (getWidth() - padding * 2 - labelPadding) / (xCoordinates.size() - 1) + padding + labelPadding;
                    int y0 = getHeight() - padding - labelPadding;
                    int y1 = y0 - pointWidth;

                    if ((i % ((int) ((xCoordinates.size() / 20.0)) + 1)) == 0) {
                        graphics.setColor(gridColor);
                        graphics.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x0, padding);
                        graphics.setColor(Color.BLACK);
                        String xLabel = i + "";
                        FontMetrics metrics = graphics.getFontMetrics();
                        int labelWidth = metrics.stringWidth(xLabel);
                        graphics.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                    }

                    graphics.drawLine(x0, y0, x0, y1);
                }
            }

            // create x and y axes
            graphics.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
            graphics.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

            Stroke oldStroke = graphics.getStroke();
            graphics.setColor(lineColor);
            graphics.setStroke(GRAPH_STROKE);
            for (int i = 0; i < graphPoints.size() - 1; i++) {
                int x1 = graphPoints.get(i).x;
                int y1 = graphPoints.get(i).y;
                int x2 = graphPoints.get(i + 1).x;
                int y2 = graphPoints.get(i + 1).y;
                graphics.drawLine(x1, y1, x2, y2);
            }

            graphics.setStroke(oldStroke);
            graphics.setColor(pointColor);

            for (Point graphPoint : graphPoints) {
                int x = graphPoint.x - pointWidth / 2;
                int y = graphPoint.y - pointWidth / 2;
                graphics.fillOval(x, y, pointWidth, pointHeight);
            }
        }

        private double getMinScore() {
            double minScore = Double.MAX_VALUE;
            for (Double score : yCoordinates) {
                minScore = Math.min(minScore, score);
            }

            return minScore;
        }

        private double getMaxScore() {
            double maxScore = Double.MIN_VALUE;
            for (Double score : yCoordinates) {
                maxScore = Math.max(maxScore, score);
            }

            return maxScore;
        }

        public void setScores(List<Double> xCoordinates, List<Double> yCoordinates) {
            this.xCoordinates = xCoordinates;
            this.yCoordinates = yCoordinates;
            invalidate();
            this.repaint();
        }

        public List<List<Double>> getScores() {
            return new ArrayList<>(List.of(xCoordinates, yCoordinates));
        }

        public void addTitle(String wantedTitle) {
            TitledBorder title = BorderFactory.createTitledBorder(new EmptyBorder(0, 0, 0, 10), wantedTitle);
            title.setTitleJustification(TitledBorder.CENTER);
            title.setTitleFont(new Font("Arial", Font.BOLD, 20));
            this.setBorder(title);
        }

        private static void createAndShowGui(List<Double> xCoordinates, List<Double> yCoordinates, File imgFile) throws IOException {
            utils.DrawingUtils.GraphPanel.MainPanel mainPanel = new utils.DrawingUtils.GraphPanel.MainPanel(xCoordinates, yCoordinates, "Consensus Result");
            mainPanel.setPreferredSize(new Dimension(1000, 800));

            JFrame frame = new JFrame("Consensus Result");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(mainPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            Container content = frame.getContentPane();
            BufferedImage img = new BufferedImage(frame.getWidth() - 15, frame.getHeight() - 36, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            content.printAll(g2d);
            g2d.dispose();
            ImageIO.write(img, "png", imgFile);
        }

        static class MainPanel extends JPanel {
            public MainPanel(List<Double> xCoordinates, List<Double> yCoordinates, String graphTitle) {
                setLayout(new BorderLayout());

                JLabel title = new JLabel(graphTitle);
                title.setFont(new Font("Arial", Font.BOLD, 25));
                title.setHorizontalAlignment(JLabel.CENTER);

                JPanel graphPanel = new utils.DrawingUtils.GraphPanel(xCoordinates, yCoordinates, null);

                utils.DrawingUtils.GraphPanel.VerticalPanel verticalPanel = new utils.DrawingUtils.GraphPanel.VerticalPanel("Probability");

                utils.DrawingUtils.GraphPanel.HorizontalPanel horizontalPanel = new utils.DrawingUtils.GraphPanel.HorizontalPanel("Expected Messages");

                add(title, BorderLayout.NORTH);
                add(horizontalPanel, BorderLayout.SOUTH);
                add(verticalPanel, BorderLayout.WEST);
                add(graphPanel, BorderLayout.CENTER);

            }
        }

        static class HorizontalPanel extends JPanel {
            final String label;

            public HorizontalPanel(String label) {
                setPreferredSize(new Dimension(0, 25));
                this.label = label;
            }

            @Override
            public void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D gg = (Graphics2D) g;
                gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Font font = new Font("Arial", Font.BOLD, 15);

                FontMetrics metrics = g.getFontMetrics(font);
                int width = metrics.stringWidth(label);

                gg.setFont(font);

                gg.drawString(label, (getWidth() - width) / 2, 11);
            }
        }

        static class VerticalPanel extends JPanel {
            final String label;

            public VerticalPanel(String label) {
                setPreferredSize(new Dimension(25, 0));
                this.label = label;
            }

            @Override
            public void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D gg = (Graphics2D) g;
                gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Font font = new Font("Arial", Font.BOLD, 15);

                FontMetrics metrics = g.getFontMetrics(font);
                int width = metrics.stringWidth(label);

                gg.setFont(font);

                drawRotate(gg, getWidth(), (getHeight() + width) / 2.0, 270, label);
            }

            public void drawRotate(Graphics2D gg, double x, double y, int angle, String text) {
                gg.translate((float) x, (float) y);
                gg.rotate(Math.toRadians(angle));
                gg.drawString(text, 0, 0);
                gg.rotate(-Math.toRadians(angle));
                gg.translate(-(float) x, -(float) y);
            }
        }

        public void run() {
            SwingUtilities.invokeLater(() -> {
                try {
                    createAndShowGui(this.xCoordinates, this.yCoordinates, graphFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void main(String[] args) throws IOException, GraphException {
        String result = "result file path";
        String imgDest = "desired file destination path";
        DrawingUtils drawingUtils = new DrawingUtils();
        drawingUtils.drawGraph(result, imgDest);
    }
}
