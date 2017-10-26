package ch.bfh.leap;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import ij.plugin.DICOM;

public class ImagePrep {
	private static List<File> files = new ArrayList<File>();
	private static ObservableList<String> names = FXCollections.observableArrayList();
	
	
	public static List<String> select(){
		files = new ArrayList<File>();
		final Stage window = new Stage();
		window.setTitle("Select images...");
		
		final FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new ExtensionFilter("DICOM Files", "*.dcm"));
		
		final DirectoryChooser dirChooser = new DirectoryChooser();
		
		final ListView<String> listView = new ListView<String>(names);
		listView.setItems(names);
		listView.setEditable(true);
		
		final Button addButton = new Button("Add image");
		addButton.setOnAction(e -> {
			File file = fileChooser.showOpenDialog(window);
			if (file != null) {
				files.add(file);
				names.add(file.getName());
			}
		});
		
		final Button addMultipleButton = new Button("Add images");
		addMultipleButton.setOnAction(e -> {
			List<File> list = fileChooser.showOpenMultipleDialog(window);
			if (list != null) {
				for (File file : list) {
					files.add(file);
					names.add(file.getName());
				}
			}
		});
		
		final Button openButton = new Button("Open...");
		openButton.setOnAction(e-> {
			File dir = dirChooser.showDialog(window);
			for(File file : dir.listFiles()) {
				if(file.getName().contains(".dcm")) {
					files.add(file);
					names.add(file.getName());
				}
			}
			
		});
		
		final Button removeButton = new Button("Remove");
		removeButton.setOnAction(e -> {
			List<String> selected = listView.getSelectionModel().getSelectedItems();
			for(String s : selected) {
				int index = names.indexOf(s);
				names.remove(s);
				files.remove(index);
			}
		});
		
		final Button startButton = new Button("Start");
		startButton.setOnAction(e-> {
			window.close();
		});
		
		final GridPane pane = new GridPane();
		
		GridPane.setConstraints(addButton, 0,1);
		GridPane.setConstraints(addMultipleButton, 0,2);
		GridPane.setConstraints(removeButton, 0,3);
		GridPane.setConstraints(startButton, 1,4);
		GridPane.setConstraints(openButton, 1, 2);
	
		pane.getChildren().addAll(listView, addButton, addMultipleButton, removeButton, startButton, openButton);
		Scene scene = new Scene(pane);
		window.setScene(scene);
		window.showAndWait();
		
		List<String> paths = new ArrayList<String>();
		for(File file : files) {
			paths.add(file.getAbsolutePath());
		}
		
		return paths;
	}
	
}
