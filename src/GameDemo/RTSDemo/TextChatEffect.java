package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.Commands.CommandHandler;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * this is an independent effect that represents a game chat window similar to that from league of legends or sc2.
 * 
 * When the player presses enter, this will appear on the screen. It will be rectangular with chat history on top and current text input at the bottom.
 * the player can type into it and send messages, adding them to the chat history. 
 * @author guydu
 */
public class TextChatEffect extends IndependentEffect{
    public static String localChatAlias = "LocalUser";
    public int messageLifespan = RTSGame.desiredTPS * 4; // display commands up to this many ticks back
    private String textAreaContents = ""; // current text area state. where the user types.
    private Game game;
    private boolean isOpen = false;
    
    public Coordinate locationOnScreen = new Coordinate(10, 500);
    public int height = 200;
    public int width = 460;
    
    private HashMap<Long, ArrayList<ChatMessage>> chatHistoryMap = new HashMap<>();
    
    public void submitCurrentText() {
        long chatTick = game.getGameTickNumber();
        ChatMessage message = new ChatMessage(textAreaContents, chatTick, localChatAlias);
        if(ExternalCommunicator.isMultiplayer) {
            ExternalCommunicator.sendMessage(message.toMpString());
        }
        addChatMessageToHistory(message);
    }
    
    public void addChatMessageToHistory(ChatMessage message) {
        var tickList = chatHistoryMap.getOrDefault(message.tick, new ArrayList<ChatMessage>());
        tickList.add(message);
        chatHistoryMap.put(message.tick, tickList);
    }
    
    public static class ChatMessage implements Serializable{
        public String text;
        public long tick;
        public String senderAlias;
        public ChatMessage (String text, long tick, String sender) {
            this.text = text;
            this.tick = tick;
            this.senderAlias = sender;
        }
        public ChatMessage (String mpString) {
            var chunks = mpString.substring(5).split("~||||~");
            this.senderAlias = chunks[0];
            this.text = chunks[2];
            this.tick = Long.parseLong(chunks[1]);
        }
        public String toMpString() {
            return "chat:" + senderAlias + "~||||~" + tick + "~||||~" + text;
        }
    }
    
    public TextChatEffect(Game g) {
        game = g;
    }
    
    @Override
    public void onPostDeserialization(Game g) {
        this.game = g;

        // Update static reference so other code uses the new deserialized instance
        RTSGame.textChatEffect = this;
    }

    @Override
    public void render(Graphics2D g) {
        // todo
        }

    @Override
    public void tick() {
        //todo
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public String getTextAreaContents() {
        return textAreaContents;
    }

    /**
     * returns chat history ordered by ticknumber sent
     * @return ordered by tickNumber
     */
    public ArrayList<ChatMessage> getChatHistory() {
        ArrayList<ChatMessage> chats = new ArrayList<>();
        chatHistoryMap.keySet().forEach(tickNum -> {
            var tickChats = chatHistoryMap.get(tickNum);
            chats.addAll(tickChats);
        });
        return chats;
    }
    
    /**
     * returns chat messages with tick numbers greater than sent threshold
     * @param tickThreshold tick cutoff
     * @return list of messages
     */
    public ArrayList<ChatMessage> getChatHistorySinceTick(long tickThreshold) {
        ArrayList<ChatMessage> out = new ArrayList<>();
        var keyset = chatHistoryMap.keySet().toArray();
        for(int i = keyset.length-1; i >= 0; i--) {
            if(i > tickThreshold) {
                out.addAll(chatHistoryMap.get(keyset[i]));
            }
        }
        return out;
    }
    
    
    /**
     * updates the current textbox contents. If setting to a truthy value, also set the thing as open.
     * @param textAreaContents 
     */
    public void setTextAreaContents(String textAreaContents) {
        if(textAreaContents == null) textAreaContents = "";
        this.textAreaContents = textAreaContents;
        if(textAreaContents.length() > 1) {
            setIsOpen(true);
        }
    }
    
}
