import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;


public class ImageSearcher {
    private static String outputDir = "./output/";
    private static String datasetDir = "./DataSet/";
    private static int resultNum = 30;
    private ArrayList<String> imageFile, queryFile;
    private ArrayList<double[]> imgBins, queryBins;
    private int[] partition = new int[3];
    private int binsNum;

    public void buildImgHistogram(String allImgFilename, int binsNum) {
        this.binsNum = binsNum;
        if (binsNum == 16) {
            partition[0] = 2;
            partition[1] = 4;
            partition[2] = 2;
        } else if (binsNum == 128) {
            partition[0] = 4;
            partition[1] = 8;
            partition[2] = 4;
        } else {
            System.out.println(binsNum + "is not supported. Please use 16 or 128 only.");
            return;
        }
        imageFile = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(allImgFilename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                if(line.trim().isEmpty()) {
                    continue;
                }
                String[] words = line.trim().split(" ");
                imageFile.add(datasetDir + words[0]);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imgBins = new ArrayList<double[]>();
        for (int i = 0; i < imageFile.size(); i++) {
            imgBins.add(calImageBins(imageFile.get(i)));
        }
    }

    private void readQuery(String queryFilename) {
        queryFile = new ArrayList<String>();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(queryFilename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = null;
        try {
            while((line = reader.readLine()) != null) {
                if(line.trim().isEmpty()) {
                    continue;
                }
                String[] words = line.trim().split(" ");
                queryFile.add(datasetDir + words[0]);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double[] calImageBins(String filename) {
        File file = new File(filename);
        BufferedImage img = null;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        double bins[] = new double[binsNum];
        int width = img.getWidth();
        int height = img.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = img.getRGB(i, j); // 0xAARRGGBB
                int b = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                
                r /= (256 / partition[0]);
                g /= (256 / partition[1]);
                b /= (256 / partition[2]);

                bins[r + g * partition[0] + b * (partition[1] * partition[2])] ++;
            }
        }

        for (int i = 0; i < binsNum; i++) {
            bins[i] /= width * height;
        }
        return bins;
    }

    private double calL2(double[] p, double [] q) {
        double sum = 0;
        for (int i = 0; i < p.length; i++) {
            sum += (p[i] - q[i]) * (p[i] - q[i]);
        }
        return Math.sqrt(sum);
    }
    
    private double calHI(double[] p, double[] q) {
        double sum1 = 0, sum2 = 0;
        for (int i = 0; i < p.length; i++) {
            sum1 += Math.min(p[i], q[i]);
            sum2 += q[i];
        }
        return 1 - sum1/sum2;
    }

    private double calBh(double[] p, double[] q) {
        double sum = 0;
        for (int i = 0; i < p.length; i++) {
            sum += Math.sqrt(p[i] * q[i]);
        }
        double tmp = 1 - sum;
        return tmp > 0 ? Math.sqrt(tmp) : 0;
    }

    private static double calChi(double[] p, double[] q) {
        double sum = 0;
        for(int i = 0; i < p.length; i++) {
            if(p[i] + q[i] != 0) {
                sum = sum + (p[i] - q[i]) / (p[i] + q[i]) * (p[i] - q[i]);
            }
        }
        return sum;
    }



    class PathDistPair implements Comparable<PathDistPair> {
        String path;
        String subdir;
        Double dist;
        public PathDistPair(String path, double dist) {
			this.path = path;
			this.dist = dist;
            subdir = path.substring(datasetDir.length());
            subdir = subdir.substring(0, subdir.indexOf('/'));
		}
		public int compareTo(PathDistPair p2) {
			return dist.compareTo(p2.dist);
		}

    }
    private String getResFilename(String path) {
        String res = path.replace("jpg", "txt");
        res = res.replace("/", "_");
        res = "res_" + res;
        return res;
    }

    private void createFile(File f) {
        if (f.exists()) return;
        File dir = f.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchAll(String distType) {
        int imageFileSize = imageFile.size();
        int queryFileSize = queryFile.size();
               
        double[] accuracy = new double[queryFileSize];
        String resDir = outputDir + binsNum + "bins/" + distType +  "/";
        for (int i = 0; i < queryFileSize; i++) {
            PathDistPair[] dist = search(queryFile.get(i), distType);
            String queryImgPath = queryFile.get(i).substring(datasetDir.length());
            String subdir = queryImgPath.substring(0, queryImgPath.indexOf('/'));
            String resFilename = getResFilename(queryImgPath);
            // output the first 30 results
            String resFilepath = resDir + resFilename;
            createFile(new File(resFilepath));
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(resFilepath));
                int correctCount = 0;
                for (int j = 0; j < resultNum; j++) {
                    String filename = dist[j].path.substring(datasetDir.length());
                    writer.write(filename + " " + dist[j].dist + "\n");
                    if (subdir.equals(dist[j].subdir))
                        correctCount ++;
                }
                accuracy[i] = (double)correctCount / resultNum;
                writer.close();
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
        
        // output accuracy
        String accuracyFilePath = resDir + "res_overall.txt";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(accuracyFilePath));
            double sum = 0.0;
            for (int i = 0; i < queryFileSize; i++) {
                String queryImgPath = queryFile.get(i).substring(datasetDir.length());
                writer.write(queryImgPath + " " + accuracy[i] + "\n");
                sum += accuracy[i];
            }
            writer.write(sum / queryFileSize + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public PathDistPair[] search(String queryFilePath, String distType) {
    	double[] queryBin = calImageBins(queryFilePath);
        PathDistPair[] dist = new PathDistPair[imageFile.size()];
        for (int j = 0; j < imageFile.size(); j++) {
            double d = Double.MAX_VALUE;
            if (distType.equals("L2")) {
                d = calL2(queryBin, imgBins.get(j));
            } else if (distType.equals("HI")) {
                d = calHI(queryBin, imgBins.get(j));
            } else if (distType.equals("Bh")) {
                d = calBh(queryBin, imgBins.get(j));
            } else if (distType.equals("Chi")) {
                d = calChi(queryBin, imgBins.get(j));
            }
            dist[j] = new PathDistPair(imageFile.get(j), d);
        }
        Arrays.sort(dist);
        return dist;
    }
    public void runAll(String allImgFilename, String queryFilename, int binsNum) {
        System.out.println("building dataset histogram for " + binsNum +  " bins");
        buildImgHistogram(allImgFilename, binsNum);
        System.out.println("calculating " + binsNum +  " bins with Euclidean dist");
        readQuery(queryFilename);
        searchAll("L2");
        System.out.println("calculating " + binsNum +  " bins with Histogram Intersection dist");
        searchAll("HI");
        System.out.println("calculating " + binsNum +  " bins with Bhattacharyya distance");
        searchAll("Bh");
        System.out.println("calculating " + binsNum +  " bins with Chi-Square dist");
        searchAll("Chi");
    }

    public static void main(String[] args) {
        ImageSearcher is = new ImageSearcher();
        is.runAll("./AllImages.txt", "./QueryImages.txt", 16);
        is.runAll("./AllImages.txt", "./QueryImages.txt", 128);
    }

}
