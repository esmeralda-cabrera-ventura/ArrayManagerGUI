package ArrayLab;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;
import java.util.stream.Collectors;

public class ArrayLab extends Application {
    private final C c = new C(20);
    private final ListView<Integer> list = new ListView<>();
    private final ListView<Integer> sorted = new ListView<>();
    private final Label sizeLbl = new Label("-");
    private final Label msg = new Label();

    private final TextField addVals   = new TextField();
    private final TextField addIdx    = new TextField();
    private final TextField findVal   = new TextField();
    private final TextField delIdxOnly= new TextField();
    private final TextField delVal    = new TextField();
    private final TextField delValIdx = new TextField();

    @Override public void start(Stage s) {
        s.setTitle("Array Manager");
        list.setCellFactory(v -> new HiCell(c));

        list.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) -> {
            if (n == null || n.intValue() < 0) return;
            say("Index: " + n.intValue());
        });

        Button addBtn     = btn("Add", e -> add());
        Button delIdxBtn  = btn("Delete Index", e -> delByIndex());
        Button sortBtn    = btn("Sort → Preview", e -> sortPreview());
        Button clrPrev    = btn("Clear Preview", e -> { sorted.setItems(FXCollections.observableArrayList()); say("Preview cleared."); });
        Button findBtn    = btn("Search", e -> search());
        Button clrHi      = btn("Clear Highlights", e -> { c.clearHi(); list.refresh(); say("Highlights cleared."); });
        Button delNum     = btn("Delete Number", e -> delByValue());
        Button clearInputs= btn("Clear Inputs", e -> clearInputs());
        Button resetBtn   = btn("Reset Array", e -> resetArray());

        addVals.setPromptText("values: 5, 9, 12");
        addIdx.setPromptText("index");
        findVal.setPromptText("value");
        delIdxOnly.setPromptText("index");
        delVal.setPromptText("number");
        delValIdx.setPromptText("index (optional)");

        VBox addCard     = card("Add group @ index", new HBox(8, addVals, addIdx, addBtn));
        VBox delIdxCard  = card("Delete by index",   new HBox(8, delIdxOnly, delIdxBtn));
        VBox delNumCard  = card("Delete number",     new HBox(8, delVal, delValIdx, delNum));
        VBox searchCard  = card("Search",            new HBox(8, findVal, findBtn, clrHi));
        VBox sortCard    = card("Sort",              new HBox(8, sortBtn, clrPrev));
        VBox sizeCard    = card("Array size",        sizeLbl);
        VBox sessionCard = card("Session",           new HBox(8, clearInputs, resetBtn));

        VBox right = new VBox(10, addCard, delIdxCard, delNumCard, searchCard, sortCard, sizeCard, sessionCard);
        right.setPadding(new Insets(10));
        right.setPrefWidth(440);

        HBox lists = new HBox(12, titled("Current Array", list), titled("Sorted Preview", sorted));
        lists.setPadding(new Insets(12));

        BorderPane root = new BorderPane(lists, header("Array Manager"), right, statusBar(), null);
        refresh();

        Scene scene = new Scene(root, 930, 520);
        s.setScene(scene);
        s.show();

        root.setStyle("-fx-font-family: 'Segoe UI', 'Inter', sans-serif; -fx-background-color: #f6f7fb;");
    }

    private void add() {
        try {
            var vals = Arrays.stream(addVals.getText().trim().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .map(Integer::parseInt).toList();
            int idx = Integer.parseInt(addIdx.getText().trim());
            c.insertAll(idx, vals);
            c.clearHi(); refresh();
            say("Added " + vals.size() + " at index " + idx + ".");
        } catch (Exception ex) { warn("Check values and index."); }
    }

    private void delByIndex() {
        try {
            String t = delIdxOnly.getText().trim();
            if (t.isEmpty()) { warn("Enter an index to delete."); return; }
            int idx = Integer.parseInt(t);
            int v = c.removeAt(idx);
            c.clearHi(); refresh();
            say("Deleted " + v + " at index " + idx + ".");
        } catch (Exception ex) { warn("Bad index."); }
    }

    private void delByValue() {
        try {
            String rv = delVal.getText().trim();
            if (rv.isEmpty()) { warn("Enter the number to delete."); return; }
            int v = Integer.parseInt(rv);
            var hits = c.findAll(v);

            if (hits.isEmpty()) { say("Index: not found"); return; }
            if (hits.size() == 1) {
                int i = hits.get(0); int rem = c.removeAt(i);
                c.clearHi(); refresh(); say("Deleted " + rem + " at index " + i + "."); return;
            }

            String it = delValIdx.getText().trim();
            if (it.isEmpty()) {
                c.setHi(new HashSet<>(hits)); list.refresh();
                warn("Multiple matches. Specify index to delete. Matches at: " + hits);
                return;
            }
            int sel = Integer.parseInt(it);
            if (!hits.contains(sel)) {
                c.setHi(new HashSet<>(hits)); list.refresh();
                warn("Index does not match value. Matches at: " + hits);
                return;
            }
            int rem = c.removeAt(sel);
            c.clearHi(); refresh(); say("Deleted " + rem + " at index " + sel + ".");
        } catch (NumberFormatException ex) { warn("Bad value or index."); }
          catch (Exception ex) { warn("Unable to delete."); }
    }

    private void sortPreview() {
        var a = new ArrayList<>(c.snap());
        Collections.sort(a);
        sorted.setItems(FXCollections.observableArrayList(a));
        say("Preview sorted.");
    }

    private void search() {
        try {
            int v = Integer.parseInt(findVal.getText().trim());
            var hits = c.findAll(v);
            c.setHi(new HashSet<>(hits)); list.refresh();
            say(hits.isEmpty() ? "Index: not found"
                               : (hits.size()==1 ? "Index: " + hits.get(0)
                                                 : "Indices: " + hits.stream().map(Object::toString).collect(Collectors.joining(", "))));
        } catch (Exception ex) { warn("Bad value."); }
    }

    private void clearInputs() {
        addVals.clear(); addIdx.clear(); findVal.clear();
        delIdxOnly.clear(); delVal.clear(); delValIdx.clear();
        say("Inputs cleared.");
    }

    private void resetArray() {
        c.reset();
        c.clearHi();
        list.getSelectionModel().clearSelection();
        sorted.setItems(FXCollections.observableArrayList());
        refresh();
        say("Array reset to original.");
    }

    private void refresh() {
        ObservableList<Integer> items = FXCollections.observableArrayList(c.snap());
        list.setItems(items); list.refresh(); sizeLbl.setText(String.valueOf(c.size()));
    }

    private Label header(String title) {
        Label h = new Label(title);
        h.setPadding(new Insets(12,12,6,12));
        h.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1f2937;");
        return h;
    }
    private HBox statusBar() {
        HBox bar = new HBox(msg);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8,12,12,12));
        msg.setText("Status: Ready.");
        msg.setStyle("-fx-text-fill: #374151;");
        return bar;
    }
    private VBox titled(String t, Control c) {
        Label l = new Label(t);
        l.setStyle("-fx-font-weight: 600; -fx-text-fill: #111827;");
        VBox box = new VBox(8, l, c);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8,0,0,2);");
        if (c instanceof ListView<?> lv) { lv.setPrefWidth(300); lv.setPrefHeight(380); }
        return box;
    }
    private VBox card(String title, Node content) {
        Label l = new Label(title);
        l.setStyle("-fx-font-weight: 600; -fx-text-fill: #111827;");
        VBox box = new VBox(10, l, (content instanceof Region r ? fit(r) : content));
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8,0,0,2);");
        return box;
    }
    private Region fit(Region r) { r.setMaxWidth(Double.MAX_VALUE); return r; }

    private Button btn(String t, javafx.event.EventHandler<javafx.event.ActionEvent> h) {
        Button b = new Button(t); b.setOnAction(h);
        b.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #111827; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 6 10; -fx-border-color: #d1d5db; -fx-border-radius: 8;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: #d1d5db; -fx-text-fill: #111827; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 6 10; -fx-border-color: #cbd5e1; -fx-border-radius: 8;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #111827; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 6 10; -fx-border-color: #d1d5db; -fx-border-radius: 8;"));
        return b;
    }

    private void say(String s){ msg.setText("Status: " + s); }
    private void warn(String s){ msg.setText("Warning: " + s); }

    public static void main(String[] args){ launch(args); }

    private static class C {
        private int[] a; private int len; private int[] orig; private Set<Integer> hi = new HashSet<>();
        C(int n){
            if(n<0)n=0; a=new int[Math.max(1,n)]; len=n;
            var r=new Random(); for(int i=0;i<n;i++) a[i]=r.nextInt(100);
            orig = Arrays.copyOf(a, len);
        }
        int size(){ return len; }
        List<Integer> snap(){ var out=new ArrayList<Integer>(len); for(int i=0;i<len;i++) out.add(a[i]); return out; }
        void ensure(int need){ if(need<=a.length) return; a=Arrays.copyOf(a, Math.max(a.length*2, need)); }
        void insertAll(int idx, List<Integer> vals){
            if(idx<0||idx>len) throw new IndexOutOfBoundsException();
            int m=vals.size(); ensure(len+m);
            for(int i=len-1;i>=idx;i--) a[i+m]=a[i];
            int p=idx; for(Integer v:vals) a[p++]=v; len+=m;
        }
        int removeAt(int idx){
            if(idx<0||idx>=len) throw new IndexOutOfBoundsException();
            int v=a[idx]; for(int i=idx+1;i<len;i++) a[i-1]=a[i]; len--; return v;
        }
        List<Integer> findAll(int v){ var r=new ArrayList<Integer>(); for(int i=0;i<len;i++) if(a[i]==v) r.add(i); return r; }
        void reset(){ len = orig.length; ensure(len); System.arraycopy(orig, 0, a, 0, len); }
        void setHi(Set<Integer> s){ hi=s; }
        void clearHi(){ hi.clear(); }
        boolean isHi(int i){ return hi.contains(i); }
    }

    private static class HiCell extends ListCell<Integer>{
        private final C c; HiCell(C c){ this.c=c; }
        @Override protected void updateItem(Integer it, boolean empty){
            super.updateItem(it, empty);
            if(empty||it==null){ setText(null); setStyle(""); }
            else {
                setText(String.valueOf(it));
                setStyle(c.isHi(getIndex()) ? "-fx-background-color: #fde68a; -fx-font-weight: 700;" : "");
            }
        }
    }
}
