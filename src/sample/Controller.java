package sample;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Duration;

import java.sql.*;
import java.util.HashMap;

import static java.lang.System.exit;

public class Controller {
    @FXML
    private TextField word;

    @FXML
    private Label wordStatus;

    @FXML
    private CheckBox Noun;
    @FXML
    private CheckBox Verb;
    @FXML
    private CheckBox Adjective;
    @FXML
    private CheckBox Adverb;
    @FXML
    private CheckBox Pronoun;
    @FXML
    private CheckBox Conjunction;
    @FXML
    private CheckBox Preposition;

    @FXML
    private Button NounEdit;
    @FXML
    private Button VerbEdit;
    @FXML
    private Button AdjEdit;
    @FXML
    private Button AdvEdit;
    @FXML
    private Button ProEdit;
    @FXML
    private Button ConjEdit;
    @FXML
    private Button PrepEdit;

    @FXML
    private Label NounSucc;
    @FXML
    private Label VerbSucc;
    @FXML
    private Label AdjSucc;
    @FXML
    private Label AdvSucc;
    @FXML
    private Label ProSucc;
    @FXML
    private Label ConjSucc;
    @FXML
    private Label PrepSucc;

    @FXML
    private ComboBox displayMeanings;

    @FXML
    private ComboBox Language;
    @FXML
    private TextField Level;
    @FXML
    private Button SubmitLevelButton;
    @FXML
    private Label LevelStatus;

    @FXML
    private TableView TableExamples;
    @FXML
    private TableColumn Examples;

    @FXML
    private TextField EditMeaning;

    @FXML
    private Button InsertButton;
    @FXML
    private Button DeleteButton;
    @FXML
    private Button UpdateButton;
    @FXML
    private Label MeaningStatus;

    private static String partOfSpeech = null;
    private static int index;

    private HashMap<String, HashMap<String, Example[]>> words;
    // Local Database - Unfortunately it is hardcoded now, but it will be set later.
    private final static String DB_URL = "jdbc:mysql://localhost:3306/userinfo";
    private final static String USER = "root";
    private final static String PASS = "tBttPAtI20YA";

    private static Statement statement;

    /**
     * Creates the HashMap for words and meaning to will later be displayed on the TableView
     * Establishes a connection with the database
     */
    public Controller() {
        words = new HashMap<>();
        try {
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
        } catch (SQLException e) {
            System.out.println("Unable to establish a connection to the database.");
        }
    }

    /**
     * Exits the program successfully- It is the only functionality that having an exit button would have.
     * Uses would probably be when starting in full screen more, but beside that, I don't know.
     *
     * @param event is never used
     */
    public void handleExitButton(ActionEvent event) {
        exit(0);
    }


    /**
     * Sets all the CheckBoxes to be not selected
     * Disables all the part of speech's 'Edit' Buttons so that
     * the 'Get' Method can later reactivate them.
     * Words only belong to a few part of speeches (usually 1 or 2)
     * so having 'Get' reactivate them is faster than the other way
     */
    private void clearCheckBoxes() {
        Noun.setSelected(false);
        Verb.setSelected(false);
        Adjective.setSelected(false);
        Adverb.setSelected(false);
        Pronoun.setSelected(false);
        Conjunction.setSelected(false);
        Preposition.setSelected(false);

        NounEdit.setDisable(true);
        VerbEdit.setDisable(true);
        AdjEdit.setDisable(true);
        AdvEdit.setDisable(true);
        ProEdit.setDisable(true);
        ConjEdit.setDisable(true);
        PrepEdit.setDisable(true);
    }

    /**
     * Effectively disables the right side of the word editor by clearing
     * TextFields and disabling buttons. Options are reset as their default
     * option
     */
    private void clearWordChange() {
        TableExamples.setItems(null);
        displayMeanings.setValue(null);
        EditMeaning.setText(null);
        InsertButton.setDisable(true);
        DeleteButton.setDisable(true);
        UpdateButton.setDisable(true);
        Level.setText(null);
        SubmitLevelButton.setDisable(true);
    }

    /**
     * Creates a FadeTransition that get activated on the chosen Node
     * The Node then has it's text changed to the message.
     * Node displays red text (Crimson) for 5 seconds before fading.
     *
     * @param labeled contains buttons or labels
     * @param message contains the message to be displayed by the chosen Node
     */
    private void displayMessage(Labeled labeled, final String message) {
        FadeTransition fade = createFade(labeled);
        labeled.setText(message);
        SequentialTransition blinkThenFade = new SequentialTransition(labeled, fade);
        blinkThenFade.play();
    }

    /**
     * Creates a FadeTransition that makes a Node disappear after 5 seconds
     *
     * @param node Button or Label's text that will disappear after 5 seconds
     * @return FadeTransition that hides the Node from view
     */
    private FadeTransition createFade(Node node) {
        FadeTransition fade = new FadeTransition(Duration.seconds(5), node);
        fade.setFromValue(1);
        fade.setToValue(0);
        return fade;
    }

    /**
     * The default language is English (1st language) and the other
     * language is Chinese (2nd language). Tells the user that 'No Level
     * Set' if it is set as null. Otherwise, displays the level for English
     * on the TextField
     *
     * @param word     Word selected
     * @param language The language chosen (English or Chinese)
     * @throws SQLException is thrown if the statement is invalid
     */
    private void getLevel(String word, String language) throws SQLException {
        String query = "SELECT " + language + " FROM word WHERE word='" + word + "'";
        ResultSet rs = statement.executeQuery(query);
        int num = 0;
        if (rs.next()) {
            int level = rs.getInt(language);
            if (level == 0) {
                Level.setText(null);
                displayMessage(LevelStatus, "No level set");
                return;
            }
            num = level;
        }
        displayMessage(LevelStatus, "Level Displayed");
        Level.setText(String.valueOf(num));
    }

    /**
     * Disables portions of the word editor which are not being used (Right-side and the CheckBoxes).
     * If the word entered does not exist, the Label displays that it 'Does Not Exist'.
     * If the word exists, it checks which parts of speech it is and checks the corresponding
     * CheckBoxes, enables corresponding 'Edit' Buttons (other 'Edit' Buttons are already disabled
     * thanks to clearCheckBox()). Also displays that the parts of speech's were 'Loaded Successfully'.
     * Displays the language level for English (Set as default language).
     *
     * @param event is not used
     * @throws SQLException if the there is an error with the statements sent to the database
     */
    public void handleGetButton(ActionEvent event) throws SQLException {
        words.clear();
        clearCheckBoxes();
        clearWordChange();
        String name = word.getText();
        ResultSet rs = statement.executeQuery("SELECT word FROM word WHERE word = '" + name + "'");
        if (!rs.next()) {
            displayMessage(wordStatus, "Word Does Not Exist");
            return;
        }
        rs = statement.executeQuery("SELECT * FROM word WHERE word ='" + name + "'");
        if (rs.next()) {
            if (rs.getInt("noun") == 1) {
                Noun.setSelected(true);
                displayMessage(NounSucc, "Loaded Successfully");
                NounEdit.setDisable(false);
            }
            if (rs.getInt("verb") == 1) {
                Verb.setSelected(true);
                displayMessage(VerbSucc, "Loaded Successfully");
                VerbEdit.setDisable(false);
            }
            if (rs.getInt("adj") == 1) {
                Adjective.setSelected(true);
                displayMessage(AdjSucc, "Loaded Successfully");
                AdjEdit.setDisable(false);
            }
            if (rs.getInt("adv") == 1) {
                Adverb.setSelected(true);
                displayMessage(AdvSucc, "Loaded Successfully");
                AdvEdit.setDisable(false);
            }
            if (rs.getInt("pro") == 1) {
                Pronoun.setSelected(true);
                displayMessage(ProSucc, "Loaded Successfully");
                ProEdit.setDisable(false);
            }
            if (rs.getInt("conj") == 1) {
                Conjunction.setSelected(true);
                displayMessage(ConjSucc, "Loaded Successfully");
                ConjEdit.setDisable(false);
            }
            if (rs.getInt("pre") == 1) {
                Preposition.setSelected(true);
                displayMessage(PrepSucc, "Loaded Successfully");
                PrepEdit.setDisable(false);
            }
        }
        Language.setValue("English");
        getLevel(name, "1stlglevel");
        displayMeanings.setItems(null);
    }

    /**
     * Resets the Word Editor by disabling the Word Editor and Clearing Text
     * Finds a the previous word based on looking up the current word's windex
     * and finding a next word that has a windex closest to the current one
     * (Condition: windex has to be above 0) If there is a previous word set
     * TextField to be that word, else set Label to be 'No previous word'
     *
     * @param event is not used
     * @throws SQLException is thrown when there is an error with the statements connecting with the database
     */
    public void handlePreviousButton(ActionEvent event) throws SQLException {
        clearWordChange();
        int num = findWordIndex();
        while (--num > 0) {
            ResultSet rs = statement.executeQuery("SELECT word FROM word WHERE windex=" + num);
            if (rs.next()) {
                word.setText(rs.getString("word"));
                handleGetButton(event);
                return;
            }
        }
        displayMessage(wordStatus, "No previous word.");
    }

    /**
     * Resets the Word Editor by disabling the Right-side and Clearing Text
     * Finds the next word in the database based windex (really on position
     * in the databased, but that is based on windex). Sets the TextField
     * to be the next word. Otherwise Label is set to 'No next word.'
     *
     * @param event is not used
     * @throws SQLException occurs when statement is invalid
     */
    public void handleNextButton(ActionEvent event) throws SQLException {
        clearWordChange();
        int index = findWordIndex();
        String query = "SELECT word FROM word WHERE windex=" + ++index;
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
            word.setText(rs.getString("word"));
            handleGetButton(event);
            return;
        }
        displayMessage(wordStatus, "No next word.");
    }

    /**
     * Inserts the word entered into the TextField into the database assuming
     * the word entered is a valid word. Checks to see which Checkboxes are
     * selected and inserts the that information into the database. Based
     * on the part of speech selected, '1' is given that it is selected and '0'
     * is given if it is not selected. Another '0' follows it based on the number
     * of meanings (Each word starts off with 0 meanings)
     *
     * @param event is not used
     * @throws SQLException if statement to the database is invalid
     */
    public void handleInsertButton(ActionEvent event) throws SQLException {
        String name = word.getText();
        String query = "SELECT * FROM word WHERE word='" + name + "'";
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
            displayMessage(wordStatus, "Word already exists");
            return;
        }
        query = "INSERT INTO word VALUES (NULL, '" + name + "'";
        if (Noun.isSelected()) {
            query += ", 1, 0";
            NounEdit.setDisable(false);
        } else {
            query += ", 0, 0";
            NounEdit.setDisable(true);
        }
        if (Verb.isSelected()) {
            query += ", 1, 0";
            VerbEdit.setDisable(false);
        } else {
            query += ", 0, 0";
            VerbEdit.setDisable(true);
        }
        if (Adjective.isSelected()) {
            query += ", 1, 0";
            AdjEdit.setDisable(false);
        } else {
            query += ", 0, 0";
            AdjEdit.setDisable(true);
        }
        if (Adverb.isSelected()) {
            query += ", 1, 0";
            AdvEdit.setDisable(false);
        } else {
            query += ", 0, 0";
            AdvEdit.setDisable(true);
        }
        if (Pronoun.isSelected()) {
            query += ", 1, 0";
            ProEdit.setDisable(false);
        } else {
            query += ", 0, 0";
            ProEdit.setDisable(true);
        }
        if (Conjunction.isSelected()) {
            query += ", 1, 0";
            ConjEdit.setDisable(false);
        } else {
            query += ", 0, 0";
            ConjEdit.setDisable(true);
        }
        if (Preposition.isSelected()) {
            query += ", 1, 0";
            PrepEdit.setDisable(false);
        } else {
            query += ", 0, 0";
            PrepEdit.setDisable(true);
        }
        query += ", NULL, NULL)";
        statement.executeUpdate(query);
        displayMessage(wordStatus, "Inserted new word");
    }

    /**
     * Updates the database to reflect the change in part of speech's
     * CheckBoxes being selected. If the CheckBox has be selected
     * it now becomes a 1 and if it is unselected it becomes a 0 for
     * the part of speech in the database. It also enables or disables
     * each part of speech's 'Edit' Button based on the CheckBoxs. It
     * deletes the word's meaning in each part of speech if it exists.
     *
     * @param event is not used
     * @throws SQLException if statement to database is not valid
     */
    public void handleUpdateButton(ActionEvent event) throws SQLException {
        String query = "UPDATE word SET ";
        if (Noun.isSelected()) {
            statement.executeUpdate(query + "noun=1 WHERE word='" + word.getText() + "'");
            NounEdit.setDisable(false);
        } else {
            statement.executeUpdate(query + "noun=0, nno=0 WHERE word='" + word.getText() + "'");
            statement.executeUpdate("DELETE FROM noun_en WHERE nindex=" + findWordIndex());
            NounEdit.setDisable(true);
        }
        if (Verb.isSelected()) {
            statement.executeUpdate(query + "verb=1 WHERE word='" + word.getText() + "'");
            VerbEdit.setDisable(false);
        } else {
            statement.executeUpdate(query + "verb=0, vno=0 WHERE word='" + word.getText() + "'");
            statement.executeUpdate("DELETE FROM verb_en WHERE nindex=" + findWordIndex());
            VerbEdit.setDisable(true);
        }
        if (Adjective.isSelected()) {
            statement.executeUpdate(query + "adj=1 WHERE word='" + word.getText() + "'");
            AdjEdit.setDisable(false);
        } else {
            statement.executeUpdate(query + "adj=0, adjno=0 WHERE word='" + word.getText() + "'");
            statement.executeUpdate("DELETE FROM adj_en WHERE nindex=" + findWordIndex());
            AdjEdit.setDisable(true);
        }
        if (Adverb.isSelected()) {
            statement.executeUpdate(query + "adv=1 WHERE word='" + word.getText() + "'");
            AdvEdit.setDisable(false);
        } else {
            statement.executeUpdate(query + "adv=0,advno=0 WHERE word='" + word.getText() + "'");
            statement.executeUpdate("DELETE FROM adv_en WHERE nindex=" + findWordIndex());
            AdvEdit.setDisable(true);
        }
        if (Pronoun.isSelected()) {
            statement.executeUpdate(query + "pro=1 WHERE word='" + word.getText() + "'");
            ProEdit.setDisable(false);
        } else {
            statement.executeUpdate(query + "pro=0, prono=0 WHERE word='" + word.getText() + "'");
            statement.executeUpdate("DELETE FROM pro_en WHERE nindex=" + findWordIndex());
            ProEdit.setDisable(true);
        }
        if (Conjunction.isSelected()) {
            statement.executeUpdate(query + "conj=1 WHERE word='" + word.getText() + "'");
            ConjEdit.setDisable(false);
        } else {
            statement.executeUpdate(query + "conj=0, conjno=0 WHERE word='" + word.getText() + "'");
            statement.executeUpdate("DELETE FROM conj_en WHERE nindex=" + findWordIndex());
            ConjEdit.setDisable(true);
        }
        if (Preposition.isSelected()) {
            statement.executeUpdate(query + "pre=1 WHERE word='" + word.getText() + "'");
            PrepEdit.setDisable(false);
        } else {
            statement.executeUpdate(query + "pre=0, preno=0 WHERE word='" + word.getText() + "'");
            statement.executeUpdate("DELETE FROM prep_en WHERE nindex=" + findWordIndex());
            PrepEdit.setDisable(true);
        }
        displayMessage(wordStatus, "Word updated");
        handleGetButton(event);
    }

    /**
     * Removes the word from the database. If the word does not exist, nothing
     * happens and the Textfield is reset. Clears the checkboxes and calls
     * the Update button so that the meanings can be deleted as well.
     *
     * @param event is not used
     * @throws SQLException occurs when the statement is not valid
     */
    public void handleDeleteButton(ActionEvent event) throws SQLException {
        String query = "SELECT * FROM word WHERE word='" + word.getText() + "'";
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
            clearCheckBoxes();
            handleUpdateButton(event);
            query = "DELETE FROM word WHERE word ='" + word.getText() + "'";
            statement.executeUpdate(query);
            displayMessage(wordStatus, word.getText() + " is gone");
        } else {
            displayMessage(wordStatus, "Word does not exist");
        }
        word.setText(null);
        clearCheckBoxes();
    }

    /**
     * Finds the windex of the word in the database.
     *
     * @return the windex of each word in the database or 0 word is not in database
     * @throws SQLException occurs when the statement is not valid
     */
    private int findWordIndex() throws SQLException {
        String query = "SELECT windex FROM word WHERE word='" + word.getText() + "'";
        ResultSet rs = statement.executeQuery(query);
        int windex = 0;
        if (rs.next()) {
            windex = rs.getInt("windex");
        }
        return windex;
    }

    /**
     * Creates a HashMap and finds the meanings of the word based on
     * the part of speech selected. Gets the String and sets the meaning
     * into the array of Example. Sets the parameter index to be the word's windex.
     *
     * @param windex index at which the word is stored at (It is auto-incremented)
     * @param speech String object that contains the part of speech of the current word
     * @return HashMap of a meaning (String) as key and array of Example (class) as value
     * @throws SQLException if the statement is not valid
     */
    private HashMap<String, Example[]> loadExamples(int windex, String speech) throws SQLException {
        String query = "SELECT meaning, example1, example2, example3 FROM " + speech + " WHERE nindex=" + windex;
        ResultSet rs = statement.executeQuery(query);
        HashMap<String, Example[]> examples = new HashMap<>();
        String meanings = null;
        while (rs.next()) {
            Example[] example = new Example[3];
            meanings = rs.getString("meaning");
            String ex1 = rs.getString("example1");
            example[0] = new Example();
            example[0].setExample(ex1);
            String ex2 = rs.getString("example2");
            example[1] = new Example();
            example[1].setExample(ex2);
            String ex3 = rs.getString("example3");
            example[2] = new Example();
            example[2].setExample(ex3);
            examples.put(meanings, example);
        }
        index = windex;
        return examples;
    }

    /**
     * Allows the buttons on the Right Side of the Word Editor
     * Clears the TextField that allows users to edit the meaning
     * Tells the user to select a meaning from a list of meanings
     */
    private void allowButtons() {
        EditMeaning.setText(null);
        SubmitLevelButton.setDisable(false);
        InsertButton.setDisable(false);
        DeleteButton.setDisable(false);
        UpdateButton.setDisable(false);
        displayMeanings.setPromptText("Select a meaning: ");
    }

    /**
     * Sets up the right side of the Word Editor and sets the part of speech
     * to be noun. Passes the index to load the HashMap properly. Sets up the
     * ComboBox to display all the meanings from the database based on
     * an ObservableList
     *
     * @param event is not used
     * @throws SQLException occurs when statement is not valid
     */
    public void handleNounEditButton(ActionEvent event) throws SQLException {
        allowButtons();
        partOfSpeech = "noun_en";
        int windex = findWordIndex();
        HashMap<String, Example[]> examples = loadExamples(windex, partOfSpeech);
        words.put(word.getText(), examples);
        ObservableList<String> meaningsList = FXCollections.observableArrayList();
        meaningsList.setAll(words.get(word.getText()).keySet());
        displayMeanings.setItems(meaningsList);
    }

    /**
     * Sets up the right side of the Word Editor and sets the part of speech
     * to be verb. Passes the index to load the HashMap properly. Sets up the
     * ComboBox to display all the meanings from the database based on
     * an ObservableList
     *
     * @param event is not used
     * @throws SQLException occurs when statement is not valid
     */
    public void handleVerbEditButton(ActionEvent event) throws SQLException {
        allowButtons();
        partOfSpeech = "verb_en";
        int windex = findWordIndex();
        HashMap<String, Example[]> examples = loadExamples(windex, partOfSpeech);
        words.put(word.getText(), examples);
        ObservableList<String> meaningsList = FXCollections.observableArrayList();
        meaningsList.setAll(words.get(word.getText()).keySet());
        displayMeanings.setItems(meaningsList);
    }

    /**
     * Sets up the right side of the Word Editor and sets the part of speech
     * to be adjective. Passes the index to load the HashMap properly. Sets up the
     * ComboBox to display all the meanings from the database based on
     * an ObservableList
     *
     * @param event is not used
     * @throws SQLException occurs when statement is not valid
     */
    public void handleAdjEditButton(ActionEvent event) throws SQLException {
        allowButtons();
        partOfSpeech = "adj_en";
        int windex = findWordIndex();
        HashMap<String, Example[]> examples = loadExamples(windex, partOfSpeech);
        words.put(word.getText(), examples);
        ObservableList<String> meaningsList = FXCollections.observableArrayList();
        meaningsList.setAll(words.get(word.getText()).keySet());
        displayMeanings.setItems(meaningsList);
    }

    /**
     * Sets up the right side of the Word Editor and sets the part of speech
     * to be adverb. Passes the index to load the HashMap properly. Sets up the
     * ComboBox to display all the meanings from the database based on
     * an ObservableList
     *
     * @param event is not used
     * @throws SQLException occurs when statement is not valid
     */
    public void handleAdvEditButton(ActionEvent event) throws SQLException {
        allowButtons();
        partOfSpeech = "adv_en";
        int windex = findWordIndex();
        HashMap<String, Example[]> examples = loadExamples(windex, partOfSpeech);
        words.put(word.getText(), examples);
        ObservableList<String> meaningsList = FXCollections.observableArrayList();
        meaningsList.setAll(words.get(word.getText()).keySet());
        displayMeanings.setItems(meaningsList);
    }

    /**
     * Sets up the right side of the Word Editor and sets the part of speech
     * to be Pronoun. Passes the index to load the HashMap properly. Sets up the
     * ComboBox to display all the meanings from the database based on
     * an ObservableList
     *
     * @param event is not used
     * @throws SQLException occurs when statement is not valid
     */
    public void handleProEditButton(ActionEvent event) throws SQLException {
        allowButtons();
        partOfSpeech = "pro_en";
        int windex = findWordIndex();
        HashMap<String, Example[]> examples = loadExamples(windex, partOfSpeech);
        words.put(word.getText(), examples);
        ObservableList<String> meaningsList = FXCollections.observableArrayList();
        meaningsList.setAll(words.get(word.getText()).keySet());
        displayMeanings.setItems(meaningsList);
    }

    /**
     * Sets up the right side of the Word Editor and sets the part of speech
     * to be Conjunction. Passes the index to load the HashMap properly. Sets up the
     * ComboBox to display all the meanings from the database based on
     * an ObservableList
     *
     * @param event is not used
     * @throws SQLException occurs when statement is not valid
     */
    public void handleConjEditButton(ActionEvent event) throws SQLException {
        allowButtons();
        partOfSpeech = "conj_en";
        int windex = findWordIndex();
        HashMap<String, Example[]> examples = loadExamples(windex, partOfSpeech);
        words.put(word.getText(), examples);
        ObservableList<String> meaningsList = FXCollections.observableArrayList();
        meaningsList.setAll(words.get(word.getText()).keySet());
        displayMeanings.setItems(meaningsList);
    }

    /**
     * Sets up the right side of the Word Editor and sets the part of speech
     * to be Preposition. Passes the index to load the HashMap properly. Sets up the
     * ComboBox to display all the meanings from the database based on
     * an ObservableList
     *
     * @param event is not used
     * @throws SQLException occurs when statement is not valid
     */
    public void handlePrepEditButton(ActionEvent event) throws SQLException {
        allowButtons();
        partOfSpeech = "prep_en";
        int windex = findWordIndex();
        HashMap<String, Example[]> examples = loadExamples(windex, partOfSpeech);
        words.put(word.getText(), examples);
        ObservableList<String> meaningsList = FXCollections.observableArrayList();
        meaningsList.setAll(words.get(word.getText()).keySet());
        displayMeanings.setItems(meaningsList);
    }

    /**
     * Gets the meaning chosen from the ComboBox and gets the examples
     * form the HashMap. Loads the data into the the TableView. Sets the TableView
     * to be editable and updates any changes to reflect the changes made on
     * the table. Updates the Editing Area.
     *
     * @param event is not used
     */
    public void handleMeaningOption(ActionEvent event) {
        String meaning = (String) displayMeanings.getValue();
        if (meaning == null) {
            TableExamples.setItems(null);
            return;
        }
        ObservableList<Example> data = FXCollections.observableArrayList();
        Examples.setCellValueFactory(new PropertyValueFactory<Example, String>("example"));
        Example[] example = words.get(word.getText()).get(meaning);
        data.add(example[0]);
        data.add(example[1]);
        data.add(example[2]);
        TableExamples.setItems(data);
        TableExamples.setEditable(true);
        TableExamples.getSelectionModel().selectFirst();
        Examples.setCellFactory(TextFieldTableCell.forTableColumn());
        EditMeaning.setText(meaning);
    }

    /**
     * Inserts a new meaning for the word in the correct part of speech's database.
     * Checks to see if the meaning is a valid one or if it is already exists. Inserts
     * the meaning into the database and increased the number of meanings based on the
     * part of speech by one. Inserts the new meaning and empty examples into the HashMap
     * and resets the TableView
     *
     * @param event is not used
     * @throws SQLException occurs when statement is not valid
     */
    public void handleInsertMeaning(ActionEvent event) throws SQLException {
        if (EditMeaning.getText() == null) {
            displayMessage(MeaningStatus, "Enter a meaning");
            return;
        }
        String newMeaning = EditMeaning.getText();
        String query = "SELECT * FROM " + partOfSpeech + " WHERE meaning=\"" + newMeaning + "\" && nindex=" + index;
        ResultSet rs = statement.executeQuery(query);
        if (rs.next()) {
            displayMessage(MeaningStatus, "Meaning already exists");
            return;
        }
        query = "SELECT no FROM " + partOfSpeech + " WHERE nindex=" + index + " ORDER BY no DESC";
        rs = statement.executeQuery(query);
        int num = 0;
        if (rs.next()) {
            num = rs.getInt("no");
        }
        query = "INSERT INTO " + partOfSpeech + " VALUES (" + index + ", " + ++num + ", '" + newMeaning + "', 'No meaning set'," +
                "'No meaning set', 'No meaning set')";
        statement.executeUpdate(query);
        Example[] examples = {new Example(), new Example(), new Example()};
        HashMap<String, Example[]> newExample = new HashMap<>();
        newExample.put(newMeaning, examples);
        words.put(word.getText(), newExample);
        query = "UPDATE word SET ";
        switch (partOfSpeech) {
            case "noun_en":
                handleNounEditButton(event);
                query += "nno=nno+1 WHERE windex=" + index;
                break;
            case "verb_en":
                handleVerbEditButton(event);
                query += "vno=vno+1 WHERE windex=" + index;
                break;
            case "adj_en":
                handleAdjEditButton(event);
                query += "adjno=adjno+1 WHERE windex=" + index;
                break;
            case "adv_en":
                handleAdvEditButton(event);
                query += "advno=advno+1 WHERE windex=" + index;
                break;
            case "pro_en":
                handleProEditButton(event);
                query += "prono=prono+1 WHERE windex=" + index;
                break;
            case "conj_en":
                handleConjEditButton(event);
                query += "conjno=conjno+1 WHERE windex=" + index;
                break;
            default:
                handlePrepEditButton(event);
                query += "preno=preno+1 WHERE windex=" + index;
                break;
        }
        statement.executeUpdate(query);
        displayMeanings.setValue(newMeaning);
        displayMessage(MeaningStatus, "Inserted new meaning");
    }

    /**
     * Deletes a word's meaning based on the part of speech. Checks to see if
     * the meaning exists and does nothing if it does not. Removes the meaning from the
     * database. Removes the meaning from the HashMap and clears the TableView. Decrements
     * each word's number of meanings for the part of speech by one.
     *
     * @param event is not used
     * @throws SQLException if statement is invalid
     */
    public void handleDeleteMeaning(ActionEvent event) throws SQLException {
        if (EditMeaning.getText() == null) {
            displayMessage(MeaningStatus, "No meaning to delete");
            return;
        }
        String deleteMeaning = EditMeaning.getText();
        String query = "SELECT no FROM " + partOfSpeech + " WHERE nindex=" + index + " && meaning='" + deleteMeaning + "'";
        ResultSet rs = statement.executeQuery(query);
        int numMeaning = 0;
        if (rs.next()) {
            numMeaning = rs.getInt("no");
        }
        query = "UPDATE " + partOfSpeech + " SET no=no-1 WHERE nindex=" + index + " && no>" + numMeaning;
        statement.executeUpdate(query);
        query = "DELETE FROM " + partOfSpeech + " WHERE meaning='" + deleteMeaning + "' && nindex=" + index;
        statement.executeUpdate(query);
        words.remove(deleteMeaning);
        query = "UPDATE word SET ";
        words.remove(deleteMeaning);
        switch (partOfSpeech) {
            case "noun_en":
                handleNounEditButton(event);
                query += "nno=nno-1 WHERE windex=" + index;
                break;
            case "verb_en":
                handleVerbEditButton(event);
                query += "vno=vno-1 WHERE windex=" + index;
                break;
            case "adj_en":
                handleAdjEditButton(event);
                query += "adjno=adjno-1 WHERE windex=" + index;
                break;
            case "adv_en":
                handleAdvEditButton(event);
                query += "advno=advno-1 WHERE windex=" + index;
                break;
            case "pro_en":
                handleProEditButton(event);
                query += "prono=prono-1 WHERE windex=" + index;
                break;
            case "conj_en":
                handleConjEditButton(event);
                query += "conjno=conjno-1 WHERE windex=" + index;
                break;
            default:
                handlePrepEditButton(event);
                query += "preno=preno-1 WHERE windex=" + index;
                break;
        }
        statement.executeUpdate(query);
        displayMeanings.setValue(null);
        EditMeaning.setText(null);
        displayMessage(MeaningStatus, "Deleted meaning");
    }

    /**
     * Sets each word's language level based on the language selected
     * English is the 1stlglevel and Chinese is the 2ndlglevel
     * Level must be greater than 0. No limit on max level.
     * Updates the database to reflect the level and language.
     * Displays on a Label if a language level has been updated
     * or if something went wrong.
     *
     * @param event is not used
     * @throws SQLException occurs when statement is not vlaid
     */
    public void handleEnterLevel(ActionEvent event) throws SQLException {
        String lang = (String) Language.getValue();
        int level;
        try {
            level = Integer.parseInt(Level.getText());
        } catch (NumberFormatException e) {
            displayMessage(LevelStatus, "Must be a number");
            return;
        }
        if (level <= 0) {
            displayMessage(LevelStatus, "Greater than zero");
            return;
        }
        String query = "UPDATE word SET ";
        if (lang == null) {
            displayMessage(LevelStatus, "Must choose a language");
            return;
        } else if (lang.equals("English")) {
            query += "1stlglevel=" + level + " WHERE windex=" + index;
            displayMessage(LevelStatus, "Updated English Level");
        } else if (lang.equals("Chinese")) {
            query += "2ndlglevel=" + level + " WHERE windex=" + index;
            displayMessage(LevelStatus, "Updated Chinese Level");
        }
        statement.executeUpdate(query);
    }

    /**
     * Based on the language selected it calls the getLevel() method with
     * certain parameters. English is the 1st language and Chinese is the 2nd
     * language
     *
     * @param event is not used
     * @throws SQLException if getValue()'s statement is invalid
     */
    public void handleLanguageOption(ActionEvent event) throws SQLException {
        if (Language.getValue().equals("English")) {
            getLevel(word.getText(), "1stlglevel");
        } else {
            getLevel(word.getText(), "2ndlglevel");
        }
    }

    /**
     * Updates the database to reflect the changes made on the TableView.
     * First updates the Cell to commit to the changes and then finds out which
     * example was updated. Updates the data to the correct word's meaning
     * based on index and the part of speech. After the edit has been
     * committed, it selects the cell below it.
     *
     * @param event gives the new value stored in the cell
     * @throws SQLException is thrown when statement is invalid
     */
    public void handleEdit(TableColumn.CellEditEvent<Example, String> event) throws SQLException {
        event.getTableView().getItems().get(event.getTablePosition().getRow()).setExample(event.getNewValue());
        int i = event.getTablePosition().getRow();
        String query = "UPDATE " + partOfSpeech + " SET ";
        if (i == 0) {
            words.get(word.getText()).get(displayMeanings.getValue())[0].setExample(event.getNewValue());
            query += "example1=\"" + event.getNewValue() + "\" WHERE meaning='" + displayMeanings.getValue() + "' && nindex=" + index;
            TableExamples.getSelectionModel().selectBelowCell();
        } else if (i == 1) {
            words.get(word.getText()).get(displayMeanings.getValue())[1].setExample(event.getNewValue());
            query += "example2=\"" + event.getNewValue() + "\" WHERE meaning='" + displayMeanings.getValue() + "' && nindex=" + index;
            TableExamples.getSelectionModel().selectBelowCell();
        } else {
            words.get(word.getText()).get(displayMeanings.getValue())[2].setExample(event.getNewValue());
            query += "example3=\"" + event.getNewValue() + "\" WHERE meaning='" + displayMeanings.getValue() + "' && nindex=" + index;
        }
        statement.executeUpdate(query);
    }

    /**
     * Updates the meaning for the selected word's part of speech
     * @param actionEvent Event
     * @throws SQLException Thrown with issue with query
     */
    public void handleUpdateMeaning(ActionEvent actionEvent) throws SQLException {
        String query = "UPDATE " + partOfSpeech + " SET meaning=\"" + EditMeaning.getText() + "\" WHERE nindex=" + index;
        statement.executeUpdate(query);
        /*
        switch (partOfSpeech) {
            case "noun_en":
                handleNounEditButton(actionEvent);
                break;
            case "verb_en":
                handleVerbEditButton(actionEvent);
                break;
            case "adj_en":
                handleAdjEditButton(actionEvent);
                break;
            case "adv_en":
                handleAdvEditButton(actionEvent);
                break;
            case "pro_en":
                handleProEditButton(actionEvent);
                break;
            case "conj_en":
                handleConjEditButton(actionEvent);
                break;
            case "prep_en":
                handlePrepEditButton(actionEvent);
                break;
        }
        */
    }
}