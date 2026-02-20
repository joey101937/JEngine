package GameDemo.RTSDemo;

import Framework.Coordinate;
import Framework.Game;
import Framework.IndependentEffect;
import GameDemo.RTSDemo.Multiplayer.ExternalCommunicator;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * this is an independent effect that represents a game chat window similar to that from league of legends or sc2.
 *
 * When the player presses enter, this will appear on the screen. It will be rectangular with chat history on top and current text input at the bottom.
 * the player can type into it and send messages, adding them to the chat history. messages last for a given number of ticks. if the game is multiplayer, it will use
 * externalCommunicator to send the chat message over the wire. external communicator should listen for this. as chat messages get closer to the cutoff, they should fade out
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

    // Cursor blinking state (transient - no need to persist)
    private transient int cursorBlinkTimer = 0;
    private transient boolean cursorVisible = true;
    private static final int CURSOR_BLINK_RATE = 25; // ticks

    // UI constants
    private static final Font CHAT_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Color BG_COLOR = new Color(0, 0, 0, 160);
    private static final Color INPUT_BG_COLOR = new Color(0, 0, 0, 210);
    private static final Color BORDER_COLOR = new Color(100, 100, 100, 200);
    private static final Color SEPARATOR_COLOR = new Color(120, 120, 120, 200);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color INPUT_TEXT_COLOR = new Color(220, 220, 220);

    public void submitCurrentText() {
        if (textAreaContents.isEmpty()) {
            setIsOpen(false);
            return;
        }
        long chatTick = game.getGameTickNumber();
        ChatMessage message = new ChatMessage(textAreaContents, chatTick, localChatAlias);
        if(ExternalCommunicator.isMultiplayer) {
            ExternalCommunicator.sendMessage(message.toMpString());
        }
        addChatMessageToHistory(message);
        textAreaContents = "";
        setIsOpen(false);
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
        private static final String MP_SEPARATOR = "~~~~";
        public ChatMessage (String text, long tick, String sender) {
            this.text = text.replace(MP_SEPARATOR, "*");
            this.tick = tick;
            this.senderAlias = sender;
        }
        public ChatMessage (String mpString) {
            var chunks = mpString.substring(5).split(MP_SEPARATOR);
            this.senderAlias = chunks[0];
            this.text = chunks[2];
            this.tick = Long.parseLong(chunks[1]);
        }
        public String toMpString() {
            return "chat:" + senderAlias + MP_SEPARATOR + tick + MP_SEPARATOR + text;
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
    public void tick() {
        cursorBlinkTimer++;
        if (cursorBlinkTimer >= CURSOR_BLINK_RATE) {
            cursorBlinkTimer = 0;
            cursorVisible = !cursorVisible;
        }
    }

    @Override
    public void render(Graphics2D g) {
        long currentTick = game.getGameTickNumber();

        // Match screen-space rendering to InfoPanelEffect pattern
        double scaleAmount = 1.0 / game.getZoom();
        g.scale(scaleAmount, scaleAmount);
        Coordinate cameraOffset = game.getCamera().getWorldRenderLocation().toCoordinate();
        cameraOffset.scale(1.0 / scaleAmount);
        int x = (int) locationOnScreen.x + cameraOffset.x;
        int y = (int) locationOnScreen.y + cameraOffset.y;

        Font oldFont = g.getFont();
        g.setFont(CHAT_FONT);
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight() + 2;
        int inputBoxHeight = lineHeight + 8;
        int historyHeight = height - inputBoxHeight;

        // Collect and sort all messages by tick (oldest first)
        ArrayList<ChatMessage> allMessages = getChatHistory();
        allMessages.sort(Comparator.comparingLong(m -> m.tick));

        int maxLines = historyHeight / lineHeight;

        // When open, fill however many lines fit from history.
        // When closed, only show messages within messageLifespan.
        ArrayList<ChatMessage> recentMessages = new ArrayList<>();
        for (ChatMessage msg : allMessages) {
            if (isOpen || currentTick - msg.tick <= messageLifespan) {
                recentMessages.add(msg);
            }
        }

        if (isOpen) {
            // Panel background
            g.setColor(BG_COLOR);
            g.fillRect(x, y, width, height);

            // Input area background
            g.setColor(INPUT_BG_COLOR);
            g.fillRect(x, y + historyHeight, width, inputBoxHeight);

            // Border
            g.setColor(BORDER_COLOR);
            g.drawRect(x, y, width, height);

            // Separator between history and input
            g.setColor(SEPARATOR_COLOR);
            g.drawLine(x, y + historyHeight, x + width, y + historyHeight);
        }

        // Draw message history (visible when open, or when recent messages exist)
        if (!recentMessages.isEmpty()) {
            int startIdx = Math.max(0, recentMessages.size() - maxLines);
            for (int i = startIdx; i < recentMessages.size(); i++) {
                ChatMessage msg = recentMessages.get(i);
                long age = currentTick - msg.tick;

                // Fade out in the last 20% of lifespan when chat is closed
                float alpha = 1.0f;
                if (!isOpen) {
                    long fadeStart = (long)(messageLifespan * 0.8f);
                    if (age > fadeStart) {
                        alpha = 1.0f - (float)(age - fadeStart) / (float)(messageLifespan - fadeStart);
                        alpha = Math.max(0f, Math.min(1f, alpha));
                    }
                }
                int alphaInt = (int)(255 * alpha);
                if (alphaInt <= 0) continue;

                String line = msg.senderAlias + ": " + msg.text;
                String displayLine = truncateToFit(line, fm, width - 10);

                // Newest message at the bottom of the history area; older messages above it
                int displayIndex = recentMessages.size() - 1 - i; // 0 = newest
                int baselineY = y + historyHeight - fm.getDescent() - 2 - (displayIndex * lineHeight);

                if (!isOpen) {
                    // Subtle background behind floating text for readability
                    g.setColor(new Color(0, 0, 0, (int)(alphaInt * 0.55)));
                    g.fillRect(x, baselineY - fm.getAscent() - 1, width, lineHeight);
                }

                g.setColor(new Color(255, 255, 255, alphaInt));
                g.drawString(displayLine, x + 5, baselineY);
            }
        }

        // Draw current text input when open
        if (isOpen) {
            String inputDisplay = truncateToFitFromRight(textAreaContents, fm, width - 20);
            if (cursorVisible) inputDisplay += "|";
            g.setColor(INPUT_TEXT_COLOR);
            int inputY = y + historyHeight + fm.getAscent() + 4;
            g.drawString(inputDisplay, x + 5, inputY);
        }

        g.setFont(oldFont);
        g.scale(1.0 / scaleAmount, 1.0 / scaleAmount);
    }

    private String truncateToFit(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        while (!text.isEmpty() && fm.stringWidth(text + "...") > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

    private String truncateToFitFromRight(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        while (!text.isEmpty() && fm.stringWidth("..." + text) > maxWidth) {
            text = text.substring(1);
        }
        return "..." + text;
    }

    /** Appends a character to the input text area. */
    public void appendToTextArea(char c) {
        textAreaContents += c;
    }

    /** Removes the last character from the input text area. */
    public void backspaceTextArea() {
        if (!textAreaContents.isEmpty()) {
            textAreaContents = textAreaContents.substring(0, textAreaContents.length() - 1);
        }
    }

    /** Opens the chat input. */
    public void open() {
        setIsOpen(true);
    }

    /** Closes the chat input without submitting. */
    public void close() {
        setIsOpen(false);
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        if(isOpen) System.out.println("opening");
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
