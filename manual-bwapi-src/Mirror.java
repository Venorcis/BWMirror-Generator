package bwapi;

import java.io.*;
import java.io.File;
import java.lang.Exception;
import java.lang.UnsupportedOperationException;
import java.util.*;
import java.util.regex.Pattern;

public class Mirror {

    private Game game;

    private AIModule module = new AIModule();

    private static void extractResourceFile(String resourceFilename, String outputFilename) throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFilename);
        if (in == null)
            throw new FileNotFoundException("Resource file not found: " + resourceFilename);
        FileOutputStream out;
        try {
            out = new FileOutputStream(outputFilename);
        } catch (Exception e) {
            throw new FileNotFoundException("Could not open output file: " + outputFilename);
        }
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        in.close();
    }

    private static boolean extractAndLoadNativeLibraries() {
        try {
            System.out.println("Extracting bwapi_bridge.dll");
            extractResourceFile("bwapi_bridge.dll", "./bwapi_bridge.dll");

            System.out.println("Extracting libgmp-10.dll");
            extractResourceFile("libgmp-10.dll", "./libgmp-10.dll");

            System.out.println("Extracting libmpfr-4.dll");
            extractResourceFile("libmpfr-4.dll", "./libmpfr-4.dll");

            System.out.println("Loading native library bwapi_bridge.dll");
            System.load(new File("./bwapi_bridge.dll").getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static boolean extractBwtaDataFiles() {
        try {
            Collection<String> bwtaFilenames = ResourceList.getResources(Pattern.compile("bwapi\\-data/BWTA2/[a-zA-Z0-9]+\\.bwta"));

            System.out.println("Creating ./bwapi-data/BWTA2 directory");
            new File("./bwapi-data/BWTA2").mkdirs();

            System.out.println("Extracting " + bwtaFilenames.size() + " BWTA2 files:");
            for (String filename : bwtaFilenames) {
                System.out.println(filename);
                String outputFilename = "./" + filename;
                extractResourceFile(filename, outputFilename);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    static {
        String arch = System.getProperty("os.arch");
        if(!arch.equals("x86")){
            throw new UnsupportedOperationException("BWMirror API supports only x86 architecture.");
        }

        if (!extractAndLoadNativeLibraries())
            System.exit(1);
        if (!extractBwtaDataFiles())
            System.exit(1);

        initTables();
    }

    public Game getGame() {
        return game;
    }

    public AIModule getModule() {
        return module;
    }

    /**
     * Initializes all BWAPI constant lookup tables.
     */
    private static native void initTables();

    /**
     * Initializes a connection to Broodwar, initializes the a {@link Game} object, and dispatches
     * events to your listener as long as Broodwar is in a game. If this method is called before
     * Broodwar is running, it will keep retrying until an initial connection can be established.
     *
     * The {@link Game} instance returned by {@link #getGame()} is only valid while this method
     * is running. If your code holds a copy of this object anywhere else, do not try to use it
     * again after this method returns.
     *
     * @param autoReconnect 
     *        If true, will run an infinite loop allowing you to keep your bot running as many 
     *        subsequent matches as desired. Will automatically reconnect to a Broodwar instance
     *        if the connection is interrupted.
     *        If false, will disconnect from Broodwar and return after the first match ends 
     *        (regardless of how it ended). Will not attempt to reconnect to Broodwar if the 
     *        connection is interrupted once the first match has been started. You can call
     *        {@link #startGame} again to run another match as needed.
     */
    public void startGame(boolean autoReconnect) {
        try
        {
            System.out.println("Connecting to Broodwar...");
            reconnect();
            System.out.println("Connection successful, starting match...");

            game = getInternalGame();

            do {
                System.out.println("Waiting...");
                while (!game.isInGame()) {
                    update();
                    if (!isConnected()) {
                        System.out.println("Reconnecting...");
                        reconnect();
                    }
                }

                System.out.println("Game ready!!!");

                while (game.isInGame()) {
                    processGameEvents();

                    update();
                    if (!isConnected()) {
                        System.out.println("Reconnecting...");
                        reconnect();
                    }
                }

                System.out.println("Match ended.");
            } while(autoReconnect);

            System.out.println("Finished. Disconnecting from Broodwar...");
        } catch (InterruptedException e) {
            System.out.println("Interrupted. Disconnecting from Broodwar...");
        }
        if (isConnected())
            disconnect();

        game = null;
    }

    private void reconnect() throws InterruptedException {
        while (!connect()) {
            Thread.sleep(1000);
        }
    }

	/**
	 * Returns the current connection state to a running Broodwar instance.
	 */
    public static native boolean isConnected();

    private static native boolean connect();

    private static native void disconnect();

    private static native void update();

    private native Game getInternalGame();

    private native void processGameEvents();
}
