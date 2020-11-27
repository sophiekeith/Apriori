import java.util.Scanner;
import java.io.*; 
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
public class Apriori 
{
	public static void main(String [] args){
		//Proper command line:
		//First: compile with javac Apriori.java
		//Second: execute with java Apriori database.txt alpha output.txt
		//Reads in the arguments from the command line
        String database_text = args[0];
        String alpha_string = args[1];
        String output_text = args[2];
        Float alpha_float = Float.parseFloat(alpha_string);
        int [][] trans_array = null;
        trans_array = read_database(database_text);
        apriori(trans_array, alpha_float, output_text);
    }	

    //This method stores the transactions into a matrix data structure
    //@param database_text is the .txt file containing the transactions
    //@returns trans_array which is an array of arrays
    public static int[][] read_database(String database_text){
    	String[] trans_items = null;
    	int[][] trans_array = null;
    	//The following try and catches opens the folder and read from following lines
    	File file = new File(database_text);
    	try
    	{
    		BufferedReader br = new BufferedReader(new FileReader(file));
    		//Get first line which specifies transactions and unique items
	    	try
	    	{
	    		//Read the first line to get transactions and unique items
	    		String first_string = br.readLine();
	    		trans_items = first_string.split("\\s+");

	    		//Storage for transactions items is an array of arrays
	    		int num_trans = Integer.parseInt(trans_items[0]);
	    		num_trans++; //We are adding one because the first array is storing the unique items
	    		trans_array = new int[num_trans][]; 
	    		int unique_items = Integer.parseInt(trans_items[1]);
	    		trans_array[0] = new int[unique_items];
                trans_array[0][0] = num_trans;
                trans_array[0][1] = unique_items;

		    	//Begin reading in the following transactions into corresponding arrays
		    	String st;
		    	//Iterator i serves as placeholder for the trans_array, or the array holding transactions
		    	int i =1;
		    	//Continue reading through file until all lines, or transactions, are complete
		    	while ((st = br.readLine()) != null)
		    	{		
		    			//Current_trans_string serves as the current transaction represented as string
		    			String [] current_trans_string = st.split("\\s+");
		    			int [] current_trans_int = new int[current_trans_string.length];
		    			//Current_trans_int serves as the current transaction represented as int
		    			//Must iterate through current_trans_string to transfer each item to integer form
		    			for (int k = 0; k<current_trans_string.length; k++){
		    				current_trans_int[k] = Integer.parseInt(current_trans_string[k]);
		    			}
		    			//Once the transaction has been converted, add it to the array of all transactions
		    			trans_array[i] = current_trans_int;
		    			i++;
		    	}
	    	}
	    	catch (IOException e)
	    	{
	    		System.out.println("No transactions");
	    	}
    	}
    	catch (FileNotFoundException ex)
    	{
    		System.out.println("Cannot open file");
    	}


    	return trans_array;
    }

    //This method represents the logic according the Apriori algorithm
    //@param trans_array is the array of arrays containing all transactions
    //@param alpha is a float representing the minimum threshold
    //@param output_text is a string specifying the output text.
    //@returns Void
    public static void apriori(int[][] trans_array, Float alpha, String  output_text){
        ArrayList<ArrayList<int[]>> all_frequent_items = new ArrayList<ArrayList<int[]>>(); //Array list containing all array lists of frequent itemsets, used for output
    	ArrayList<int[]> frequent_items = new ArrayList<int[]>();
    	ArrayList<int[]> candidates = new ArrayList<int[]>();
    	ArrayList<int[]> pruned_candidates = new ArrayList<int[]>();
        HashMap<int[], Integer> count_support_map= new HashMap<int[], Integer>();
    	frequent_items = generate_F1(trans_array, alpha);
    	
    	//Iterate over f1 until empty
    	int k =2; //K is the current itemset, since frequent 1-itemsets have been calculated, start with 2
    	//while (frequent_items.size() > 0){
        while (frequent_items.size()>0){
            all_frequent_items.add(frequent_items);
            if (k == 2){
                candidates = generate_candidate(frequent_items, k); //Don't need to prune for 2 itemset
                count_support_map = count_support(candidates, trans_array, k);
                frequent_items = eliminate_candidates(count_support_map, alpha, trans_array.length-1);
                
            }
            else{
                candidates = generate_candidate(frequent_items, k);
                pruned_candidates = prune_candidates(candidates, frequent_items, k);
                count_support_map = count_support(candidates, trans_array, k);
                frequent_items = eliminate_candidates(count_support_map, alpha, trans_array.length-1);
            }
            k++;
    	
    	}
        generate_output(output_text, all_frequent_items, trans_array );//Call method that will output all frequent itemsets

    }
    //This method creates frequent 1-itemsets
    //@param trans_array is an array of arrays containing transactions
    //@returns f1_gen which is a ArrayList of integers containing the frequent 1-itemsets
    public static ArrayList<int[]>  generate_F1(int[][] trans_array, Float alpha){
    	HashMap<Integer, Integer> f1 = new HashMap<>();

    	//Iterate over all trans_array to look at each individual item
    	for(int row =1; row < trans_array.length; row++){
    		for(int col =0; col < trans_array[row].length; col++){ //For each transaction
    			int item = trans_array[row][col];
    			//If item already exists in map, increment count. Else, add it to map and store count as 1
    			if (f1.containsKey(item)){
    				f1.put(item, f1.get(item) +1);		
    			}else{
    				f1.put(item, 1);
    			}
    		}
    	}

    	ArrayList<int[]> f1_gen = new ArrayList<int[]>();
    	//Iterate over f1 comparing count to threshold and add to array to be returned
    	for(HashMap.Entry<Integer, Integer> item :f1.entrySet()){
    		int count = f1.get(item.getKey());
            int totalTrans = trans_array.length-1; //Stores the number of transactions
    		float alpha_count = (float) ((float)count / (float)(totalTrans)); //Divide the count by total items
            //Add item to f1_gen if it is above threshold
    		if (alpha_count >= alpha){
                int [] candidate = new int[1];
    			candidate[0] = item.getKey();
                f1_gen.add(candidate);
    		}
    	}
    	
    	return f1_gen;
    }
    // This method creates Lk+1 itemsets 
    //@param frequent_items is an ArrayList of integers containing the frequent_itemsets
    //@param count is the current k, meaning what itemset the function is generating
    //@returns candidates which is an ArrayList of integers containing the candidates
    public static ArrayList<int[]> generate_candidate(ArrayList<int[]>frequent_items, int count){
    	ArrayList<int[]> candidates = new ArrayList<int[]>();
    	if (count ==2){ //Special condition if count is 2
    		for(int i=0; i<frequent_items.size(); i++){ //Get current itemset
	    		for(int k= i+1; k<frequent_items.size(); k++){ //Get the next itemset, iteration is always one more than current
	    			int[]item = new int[count];
	    			item[0] = frequent_items.get(i)[0]; //Add current and next together
	    			item[1] = frequent_items.get(k)[0];
	    			candidates.add(item); //Add to candidate itemsets
	    		}
    		}
    	} else {  //Generate candidate logic is using K-2 
            for (int current = 0; current<frequent_items.size(); current++){ //Get the current itemset
                for (int next = current +1; next<frequent_items.size(); next++){ //Get the next itemset
                    int [] current_array = new int[count -2];
                    int [] next_array = new int[count -2];

                    for (int s =0; s < count-2; s++){ //Create the arrays of k-2
                        current_array[s] = frequent_items.get(current)[s];
                        next_array[s] = frequent_items.get(next)[s];
                    }

                    int item_equal =0;
                    for (int t=0; t < current_array.length; t++){ //Compare the two arrays of k-2 to see if they are equal
                        if (current_array[t] == next_array[t]){
                          item_equal++;
                        }
                    }
                    int [] candidate_array = new int[count];
                    if (item_equal == count-2){//This means the two arrays of k-2 are indeed equal, so combine them and add to candidates.
                        for (int r = 0; r < frequent_items.get(current).length; r++){
                            candidate_array[r] = frequent_items.get(current)[r];
                        }
                        candidate_array[count-1] = frequent_items.get(next)[count-2];
                        candidates.add(candidate_array);
                    }       

                    
                }
            }
        }
    	return candidates;	
    }

    //This method checks if potential candidates contain subsets that are infrequent
    //@param candidates is an arraylist of integers containing potential candidates
    //@param f_k is an arraylist of integers containing previously found itemsets -- frequent itemsets from previous iteration
    //@param count is the current k, meaning what itemset the function is generating
    //@returns pruned_candidates which is all the candidates that do not contain infrequent subsets
    public static ArrayList<int[]> prune_candidates(ArrayList<int[]> candidates, ArrayList<int[]> f_k, int count){
        ArrayList<int []> candidates_pruned = new ArrayList<int[]>();
    	for (int current_candidate = 0; current_candidate <candidates.size(); current_candidate++){ //Iterate over all potential candidates
            int [] itemset = candidates.get(current_candidate); //Retrieve first itemset
            List<Integer> intList = new ArrayList<Integer>(); //Convert array to list
            for (int i : itemset)
            {
                intList.add(i);
            }

            List<Set<Integer>> list_of_subsets = new ArrayList<>(); //This will store all of the subsets of size count -1
            getSubsets(intList, count-1, 0, new HashSet<Integer>(), list_of_subsets);
            int totalCount =0; //This stores the count for how many subsets are equal to frequent subsets
            //Now, need to look through list_of_subsets to make sure they all appear in f_k -- if not, then cannot be a candidate
            for (int k =0; k <list_of_subsets.size(); k++){
                Set<Integer> current_subset_set = list_of_subsets.get(k);//Get specific set

                int[] current_subset_array= new int[count-1];
                int s =0;
                for (Integer y: current_subset_set){ //Convert the set to array
                    current_subset_array[s] = y;
                    s++;
                 }
                 
                for (int p=0; p <f_k.size(); p++){ //iterate over f_k to see if current_subset_array is contained in it
                    String fk_string = Arrays.toString(f_k.get(p)); //Conversion to string for checking equals
                    String subset_string = Arrays.toString(current_subset_array);
                    if(fk_string.equals(subset_string)){
                        totalCount++;
                    }
                }

                  
            }
            if (totalCount == count){//Meaning all the subsets of candidates were indeed frequent, then add that candidate to pruned
                candidates_pruned.add(itemset);
            } 
        }
        
        return candidates_pruned;
    }
    //Helper function for prune candidates -- finds all subsets of certain size.
    //@param list_of_items is a list of of the items in itemset
    //@param int subset_num is the subset size 
    //@param idx is the current index in list we are located
    //@param current is the subset we are currently builiding on
    //@param all_subsets is the list of sets of integers which contains all of the subsets for a given list 
    //@param Void
    public static void getSubsets(List<Integer> list_of_items, int subset_num, int idx, Set<Integer> current,List<Set<Integer>> all_subsets){
            
        if (current.size() == subset_num) { //Once you have reached the number of items in subset, this means we have a subset - add to result, recurisve solution.
            all_subsets.add(new HashSet<>(current));
            return;
        }
        if (idx == list_of_items.size()) return; //Meaning we iterated  over all of items
        Integer x = list_of_items.get(idx); //Get current item
        current.add(x); //Add to the current set we are building
        //X is in subset
        getSubsets(list_of_items, subset_num, idx+1, current, all_subsets);
        current.remove(x);
        //X is not in subset
        getSubsets(list_of_items, subset_num, idx+1, current, all_subsets);
    }

    //This method counts the support of an itemset within the transactions
    //@param candidates is an arraylist of integers containing the potential candidates
    //@param trans_array is an array of arrays containing transactions
    //@param k is the current k, meaning what itemset the function is generating
    //@returns trans_support which is a HashMap<int[], Integer> containing each itemset and the amount of times it shows up in trans_array
    public static HashMap<int[], Integer> count_support(ArrayList<int[]>candidates, int[][] trans_array, int k){
        HashMap<int[], Integer> trans_support = new HashMap<int[], Integer>();
        for (int i = 0; i < candidates.size(); i++){
            int [] current_item = candidates.get(i);
                    //Iterate over all trans_array to look at each individual item
            for(int row =1; row < trans_array.length; row++){
                int trans_count = 0; //This will be reset for every transaction -- used to see if item is in the transaction
                for(int col =0; col < trans_array[row].length; col++){ //For each transaction
                    int item = trans_array[row][col];

                    for (int j = 0; j < current_item.length; j++){ //For each itemset
                        if (item == current_item[j]){
                            trans_count++;
                        }
                    }
                }
                if (trans_count == k){ //Once down iterating through one transaction and if it contains candidate
                    //If item already exists in map, increment count. Else, add it to map and store count as 1
                    if (trans_support.containsKey(current_item)){
                        trans_support.put(current_item, trans_support.get(current_item) +1);      
                    }else{
                        trans_support.put(current_item, 1);
                    }

                }
            }
        }
        return trans_support;
    }

    //This method removes all itemsets from the candidates that are below the given threshold
    //@param count_support is a HashMap<int[], Integer> containing the itemsets and their support within all transactions
    //@param alpha is the minimum threshold specified by user
    //@param trans_total is the total amount of transactions in data
    //@returns frequent_items, an arraylist of integer arrays containing frequent items, the new f_k
    public static ArrayList<int[]> eliminate_candidates(HashMap<int[], Integer> count_support, float alpha, int trans_total){
        ArrayList<int[]>frequent_items = new ArrayList<int[]>();
        for ( int[] key : count_support.keySet() ) { //Iterate over all itemsets
            int support = count_support.get(key); //Get specific value for certain key
            float alpha_count = (float) ((float)support / (float)(trans_total)); //Divide the count by total items
            //Add item to frequent_items if it is above threshold
            if (alpha_count >= alpha){
                frequent_items.add(key);
            }
                
        }
        return frequent_items;

    }
    //This method prints all of the frequent items to denoted output text file
    //@param output_text is a string specifying where to print out to.
    //@param itemsets is an ArrayList<ArrayList<int[]>> where all frequent itemsets are stored
    //@param trans_array is the array of arrays containing all transactions
    //@returns Void
    public static void generate_output (String output_text, ArrayList<ArrayList<int[]> >itemsets, int[][] trans_array){
        try{
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(output_text));
            String itemSize = String.valueOf(itemsets.size());
            int uniqueNums = trans_array[0][1]; //Get the number of unique items, same as input transactions
            String uniqueNumsString = String.valueOf(uniqueNums);
            writer.write(itemSize); //Write the total number of frequent itemsets
            writer.write(" ");
            writer.write(uniqueNumsString); //Write the total number of unique items
            writer.newLine();
            for (int i =0; i <itemsets.size(); i++){ //Iterate over all frequent itemsets
                writer.write("Itemset: "); //Denote new itemset for clarity when viewing file
                for (int j=0; j< itemsets.get(i).size(); j++){ //Iterate over the current frequent itemsets
                    for (int k=0; k< itemsets.get(i).get(j).length; k++){ //Iterate over the the frequent item
                        int currItem = itemsets.get(i).get(j)[k];
                        String currItemString = String.valueOf(currItem);
                        writer.write(currItemString); //Write the current item
                        if (k != itemsets.get(i).get(j).length -1 ){ //If there is another number within item, add a comma
                            writer.write(",");
                        }
                    } 
                    writer.write(" "); //Write a space between items 
                }
                writer.newLine(); //Write a line between frequent itemsets
            }
            writer.close();
        }
        catch (IOException e)
            {
                System.out.println("Cannot write to file");
            }
    }





}
