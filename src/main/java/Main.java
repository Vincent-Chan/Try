import com.opencsv.CSVWriter;

import java.io.*;
import java.util.*;
import java.lang.*;

import javax.swing.JFrame;
import java.awt.Color;

public class Main
{

    static class Vertex
    {
        int row ;
        int col ;
        Vertex parent ;

        public Vertex(int x, int y, Vertex v)
        {
            this.row = x ;
            this.col = y ;
            this.parent = v ;
        }

        public Vertex opposite_node()
        {
            if (this.row > this.parent.row)
            {
                return new Vertex(this.row + 1, this.col, this) ;
            }
            if (this.row < this.parent.row)
            {
                return new Vertex(this.row - 1, this.col, this) ;
            }

            if (this.col > this.parent.col)
            {
                return new Vertex(this.row, this.col + 1, this) ;
            }
            if (this.col < this.parent.col)
            {
                return new Vertex(this.row, this.col - 1, this) ;
            }

            return null ;
        }
    }


    public static char[][] PrimMazeGenerator()
    {
        Random rand = new Random() ;

        // if maze[i][j] = '0', it is a clear path in white color
        // if maze[i][j] = '1', it is a barrier in grey color
        // if maze[i][j] = '2', it is the starting point for the mouse Jerry
        // if maze[i][j] = '3', it is the exit point where the mouse Jerry should go to

        final int r = 30 ;
        final int c = 30 ;

        // corresponds to North, East, South, West
        final int[] row_step = {-1, 0, 1, 0} ;
        final int[] col_step = {0, 1, 0, -1} ;

        char[][] maze = new char [r][c] ;

        for (int i = 0; i < r; ++i)
        {
            for (int j = 0; j < c; ++j)
            {
                maze[i][j] = '1' ;
            }
        }

        Vertex start = new Vertex(rand.nextInt(r), 0, null) ;
        maze[start.row][start.col] = '2' ;

        ArrayList<Vertex> frontier = new ArrayList<Vertex> () ;

        for (int i = 0; i < row_step.length; ++i)
        {
            if ( (start.row + row_step[i]) >= 0 &&
                    (start.row + row_step[i]) < r &&
                    (start.col + col_step[i]) >= 0 &&
                    (start.col + col_step[i]) < c )
            {
                if (maze[start.row + row_step[i]][start.col + col_step[i]] == '0')
                {
                    continue ;
                }

                frontier.add(new Vertex(start.row + row_step[i], start.col + col_step[i], start)) ;

            }
        }

        Vertex last = null;
        Vertex current = null ;
        Vertex opposite = null ;

        while (frontier.isEmpty() == false)
        {
            current = frontier.remove( rand.nextInt( frontier.size() ) ) ;
            opposite = current.opposite_node() ;

            if (opposite.row >= 0 && opposite.row < r && opposite.col >= 0 && opposite.col < c)
            {
                if (maze[current.row][current.col] == '1' && maze[opposite.row][opposite.col] == '1') {

                    maze[current.row][current.col] = '0';
                    maze[opposite.row][opposite.col] = '0';

                    last = opposite;

                    for (int i = 0; i < row_step.length; ++i) {
                        if ((opposite.row + row_step[i]) >= 0 &&
                                (opposite.row + row_step[i]) < r &&
                                (opposite.col + col_step[i]) >= 0 &&
                                (opposite.col + col_step[i]) < c) {
                            if (maze[opposite.row + row_step[i]][opposite.col + col_step[i]] == '0') {
                                continue;
                            }

                            frontier.add(new Vertex(opposite.row + row_step[i], opposite.col + col_step[i], opposite));
                        }
                    }
                }
            }


            if (frontier.isEmpty() == true)
            {
                maze[last.row][last.col] = '0' ;
            }

        }


        boolean flag_exit_placed = false ;

        while (flag_exit_placed == false)
        {
            int temp = rand.nextInt(r) ;

            if (maze[temp][c-2] == '0')
            {
                maze[temp][c-1] = '3' ;
                flag_exit_placed = true ;
            }
        }

        return maze ;

    }

    public static void print_maze(char[][] m)
    {
        for (int i = 0; i < m.length; i++)
        {
            for (int j = 0; j < m[0].length; j++)
            {
                System.out.print(m[i][j]);
            }
            System.out.println();
        }
    }

    public static void toCSV(char[][] maze_data)
    {

        File file = new File("MazeData.csv");

        try
        {
            FileWriter outputfile = new FileWriter(file);

            CSVWriter writer = new CSVWriter(outputfile);

            String[] temporary_string_arr = new String[maze_data[0].length] ;

            List<String[]> data = new ArrayList<String[]>() ;


            for (int i = 0; i < maze_data.length; ++i)
            {
                for (int j = 0; j < maze_data[0].length; ++j)
                {
                    temporary_string_arr[j] = Character.toString(maze_data[i][j]) ;
                }

                data.add(temporary_string_arr) ;

                writer.writeAll(data); ;
                data.clear() ;
            }

            writer.close() ;
        }
        catch (IOException e)
        {
            e.printStackTrace() ;
        }
    }

    public static void main(String[] args)
    {
        char[][] output_maze = PrimMazeGenerator() ;

        print_maze(output_maze) ;

        toCSV(output_maze) ;

        Window f1= new Window(output_maze) ;

        f1.setTitle("Snake");

        f1.setSize(300,300);

        f1.setVisible(true);

        f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}