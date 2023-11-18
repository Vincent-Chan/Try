import game_algorithm.GameMapGenerator;
import game_algorithm.ShortestPathGenerator;
import game_entities.characterID;
import game_scene.LandingPageController;
import game_scene.LandingPageView;
import game_scene.MazeMapController;
import game_scene.WindowsView;
import game_states.GameState;
import game_states.GameStateController;
import game_states.Location;
import game_states.Move;
import visuals.StringResources;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class TomJerryGame {
    public final int SIDE_LENGTH =  30;
    private String MAP_FILE_PATH = "src/main/java/MazeData.csv";
    private String SP_OUTPUT_PATH = "src/main/java/shortest_path_at_beginning.csv";
    public ArrayList<ArrayList<Integer>> mazeMap;
    public Location entry;
    public Location exit;

    public WindowsView windowsView;
    public MazeMapController mazeMapController;
    public GameStateController stateController;

    public ShortestPathGenerator shortestPathGenerator;

    public int jerrySpeed = 3;
    public int tomSpeed = 3;

    public TomJerryGame(String map_file_path, String sp_output_path) throws IOException {
        this.MAP_FILE_PATH = map_file_path;
        this.SP_OUTPUT_PATH = sp_output_path;

        /**
         * Initializes the map and path generators
         * Store the generated map as csv file*/
        GameMapGenerator gameMapGenerator = new GameMapGenerator(MAP_FILE_PATH);
        gameMapGenerator.to_csv(gameMapGenerator.PrimMazeGenerator());
        shortestPathGenerator = new ShortestPathGenerator(MAP_FILE_PATH,SP_OUTPUT_PATH);

        /**For sake of consistency, load the just-saved map, initializes the entry and exit location*/
        mazeMap = readData(MAP_FILE_PATH, ",");

        for(int i = 0;i<SIDE_LENGTH;i++){
            for(int j = 0;j<SIDE_LENGTH;j++){
                if(mazeMap.get(i).get(j)==2){
                    entry = new Location(i,j);
                }

                else if (mazeMap.get(i).get(j)==3)
                    exit = new Location(i,j);
            }
        }

        /**Initialize all GUI controllers and views */

        windowsView = new WindowsView(mazeMap, entry, exit);
        mazeMapController = windowsView.getMapViewer().getController();
        mazeMapController.insertImage(entry, StringResources.jerry);
        mazeMapController.insertImage(exit, StringResources.tom);
        windowsView.setTextBillboard(StringResources.gamestarts);

        stateController = new GameStateController(mazeMap, entry, exit);

    }
    /**Read maze map data from a specified csv file
     *
     * @param file  the path of the csv file containing the maze map data
     * @param separator the separator used by the csv file
     *
     * @throws  IOException if the filepath points to invalid csv file
     * */
    public ArrayList<ArrayList<Integer>> readData(String file, String separator) throws IOException {
        int count = 0;
        ArrayList<ArrayList<Integer>> content = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                ArrayList<Integer> intList = new ArrayList<>();
                String[] strlist = line.split(separator);
                for(int i = 0;i< strlist.length;i++){
                    intList.add(Integer.parseInt(strlist[i].substring(1,2))); //input is ["1","1","1"], needs to get rid of ""
                }
                content.add(intList);
            }
        } catch (FileNotFoundException e) {
            return null;
        }
        return content;
    }

    /**Choose mode of difficulty
     * <ui>
     *     <li>Easy: PLAYER_SPEED = 3, COMPUTER_SPEED = 3</li>
     *     <li>Medium: PLAYER_SPEED = 4, COMPUTER_SPEED = 6</li>
     *     <li>Hard: PLAYER_SPEED = 6, COMPUTER_SPEED = 9</li>
     * </ui>
     * */
    public void setDifficulty(){
        String[] difficultyModes = {"Easy", "Medium", "Hard"};

        // Show the message box and get the selected difficulty mode
        int choice = JOptionPane.showOptionDialog(null, "Please select the difficulty.\n The harder the game, the more steps Tom can run in each turn!", "Game Launcher",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, new ImageIcon(StringResources.select_difficulty), difficultyModes, difficultyModes[0]);
        if (choice >= 0) {
            String selectedMode = difficultyModes[choice];
            if(selectedMode.equals("Easy")){
                jerrySpeed = 3;
                tomSpeed = 3;
            }
            else if(selectedMode.equals("Medium")){
                jerrySpeed = 4;
                tomSpeed = 6;
            }
            else{
                jerrySpeed = 6;
                tomSpeed = 9;
            }
            JOptionPane.showMessageDialog(null, "Selected Difficulty Mode: " + selectedMode);

        } else {
            // User closed the dialog or clicked outside the options
            System.exit(0);
        }
    }
    public void highlightShortestPath(){
        ShortestPathGenerator shortestPathGenerator = new ShortestPathGenerator(MAP_FILE_PATH,SP_OUTPUT_PATH);
        JOptionPane.showMessageDialog(null,StringResources.show_sp_hint , "Hint", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(StringResources.show_sp_hint_image));
        ArrayList<Location> SP = shortestPathGenerator.calculate_shortest_path(entry,exit);
        mazeMapController.highlightPath(SP, StringResources.sp_component);
        shortestPathGenerator.output_file(SP);
    }

    public void TomMovesOneStep(int remainingMoves) throws IOException {

        Location oldtom = stateController.getCharacterLocation(characterID.TOM_ID);
        Location oldjerry = stateController.getCharacterLocation(characterID.JERRY_ID);

        ArrayList<Location> shortestPathToJerry = shortestPathGenerator.calculate_shortest_path(oldtom,oldjerry);
        if(shortestPathToJerry==null){
            JOptionPane.showMessageDialog(null, "Error in generating shortestPathToJerry", "Message", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // infer intermediate move that results in location transition: oldtom->newtom
        Location newtom = shortestPathToJerry.get(1);
        Move nextMove = null;
        if(newtom.row() == oldtom.row()+1){
            nextMove = new Move.Down(characterID.TOM_ID);
        }
        if(newtom.row() == oldtom.row()-1){
            nextMove = new Move.Up(characterID.TOM_ID);
        }
        if(newtom.col() == oldtom.col()+1){
            nextMove = new Move.Right(characterID.TOM_ID);
        }
        if(newtom.col() == oldtom.col()-1){
            nextMove = new Move.Left(characterID.TOM_ID);
        }
        stateController.moveCharacter(characterID.TOM_ID,nextMove);

        Location newjerry = stateController.getCharacterLocation(characterID.JERRY_ID);
        mazeMapController.renderMap(newtom,newjerry,oldtom,oldjerry);
        windowsView.setTextBillboard(StringResources.showRemainingMoves(1, remainingMoves -1));
        windowsView.revalidate();
        windowsView.repaint();


    }
    public void TomMoves() throws IOException {
        int remainingMoves =tomSpeed;
        windowsView.setTextBillboard(StringResources.showRemainingMoves(characterID.TOM_ID,remainingMoves));

        while (remainingMoves>0){
            if(stateController.gameStateOutcome()!=GameState.CONTINUE){
                break;
            }
            this.TomMovesOneStep(remainingMoves);
            remainingMoves--;
            if(stateController.gameStateOutcome()!=GameState.CONTINUE){
                break;
            }
        }
    }
    public void JerryMoves(boolean highlighted) throws InterruptedException, IOException {
        int remainingMoves = jerrySpeed;
        windowsView.setTextBillboard(StringResources.showRemainingMoves(1, remainingMoves));
        while (remainingMoves > 0) {
            if (stateController.gameStateOutcome() != GameState.CONTINUE) {
                break;
            }
            Location oldtom = stateController.getCharacterLocation(0);
            Location oldjerry = stateController.getCharacterLocation(1);

            LinkedBlockingQueue<Move> actionQueue = windowsView.getControlPanelView().getControlPanelController().getActionQueue();
            System.out.println("Hashcode of actionqueue in main.java" + actionQueue.hashCode());
            System.out.println(actionQueue.size());
            Move nextMove = actionQueue.take();
            System.out.println("new move fetched: " + nextMove);


            /**check validity of the move*/
            boolean moveResult = stateController.moveCharacter(1, nextMove);
            if (moveResult) {
                Location newtom = stateController.getCharacterLocation(0);
                Location newjerry = stateController.getCharacterLocation(1);
                if (stateController.gameStateOutcome() != GameState.CONTINUE) {
                    break;
                }
                remainingMoves--;
                if (highlighted) {
                    mazeMapController.removeHighlightPath();
                    highlighted = false;
                }

                mazeMapController.renderMap(newtom, newjerry, oldtom, oldjerry);
                windowsView.setTextBillboard(StringResources.showRemainingMoves(1, remainingMoves));
                windowsView.revalidate();
                windowsView.repaint();

                /**clear any left-over actions in actionqueue if this is the last chance for user to move in current roun
                 * e.g. if user can move 3 steps in a round, but he presses the button for 4 times,
                 *      then there is 1 step left-over
                 *      need to clear this step before proceeding to next round
                 * */
                if (remainingMoves == 0) {
                    if (actionQueue.size() > 0) {
                        actionQueue.clear();
                    }
                }

            } else {
                JOptionPane.showMessageDialog(null, "Invalid Move!", "Message", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(StringResources.invalid_move));
            }

        }
    }
    public void run(LandingPageView landingPageView) throws IOException, InterruptedException {
        /**Player presses "Start Game"*/

        this.setDifficulty();
        windowsView.setVisible(true);
        landingPageView.setVisible(false);
        this.highlightShortestPath();
        boolean highlighted = true;

        int turn = 1; // turn%2 -> id of current GameCharacter that can move
        /**Game loop with ending condition being checked upon the record of every new state*/
        while(stateController.gameStateOutcome()== GameState.CONTINUE){
            turn = turn%2;
            if(turn == characterID.TOM_ID){
                this.TomMoves();
            }
            else{
                /**Process jerry's moves*/
                this.JerryMoves(highlighted);
                highlighted = false;
            }

            if(stateController.gameStateOutcome()!=GameState.CONTINUE){
                break;
            }
            turn++;
        }

        /**A Game Instance Ends*/
        if(stateController.gameStateOutcome() == GameState.JERRY_WIN){
            windowsView.revalidate();
            windowsView.repaint();
            JOptionPane.showMessageDialog(null, StringResources.jerryWinsMessage, "Message", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(StringResources.jerry_wins));
        }
        else{
            windowsView.revalidate();
            windowsView.repaint();
            JOptionPane.showMessageDialog(null, StringResources.tomWinsMessage, "Message", JOptionPane.INFORMATION_MESSAGE,new ImageIcon(StringResources.tom_catches_jerry));
        }
        /**Clear existing data*/
        LinkedBlockingQueue<Move> actionQueue = windowsView.getControlPanelView().getControlPanelController().getActionQueue();
        actionQueue.clear();
        windowsView.setVisible(false);

    }


}