package VaadinPortlet;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinPortlet;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 22.02.13
 * Time: 12:49
 * To change this template use File | Settings | File Templates.
 */
public class ManageChannelsWindow extends Window {
    private TextField channelCaption;
    private TextField channelURL;
    private Button addChannelButton;
    private User user;

    private static final Log log = LogFactoryUtil
            .getLog(ManageChannelsWindow.class);

    public final class RSSChannel implements Serializable {
        private final String channelName;
        private final String url;

        public RSSChannel(String channelName, String channelURL) {
            this.channelName = channelName;
            this.url = channelURL;
        }

        public String getURL() {
            return url;
        }

        public String getChannelName() {
            return channelName;
        }
    }


    private final VerticalLayout layout = new VerticalLayout();
    private final BeanItemContainer<RSSChannel> beanItemContainer;
    Thread channelFill;

    private final ProgressIndicator progressIndicator;
    private final ComboBox channelSelection;

    public ManageChannelsWindow() {
        super("Manage RSS channels");
        setModal(true);
        setSizeUndefined();

        beanItemContainer = new BeanItemContainer<RSSChannel>(
                RSSChannel.class);

        progressIndicator = new ProgressIndicator();
        channelSelection = new ComboBox("Select channel", beanItemContainer);


        user = ((MyVaadinUI)UI.getCurrent()).getUser();


        channelFill = new Thread() {
        @Override
        public void run() {
            try {
                List<RSSChannel> channelList = fetchChannels();
                    beanItemContainer.addAll(channelList);
                    updateState(true);
            } catch (Exception e) {
                log.warn("Channel list could not be filled", e);
                    layout.removeAllComponents();
                    layout.addComponent(new Label(
                            "Channel list could not be filled: "
                                    + e.getMessage()));
            } finally {
                // Release memory
                channelFill = null;
                channelSelection.setValue(channelSelection.getItemIds().iterator().next());
            }
        }

        /*private List<RSSChannel> fillChannelList() {
            List<RSSChannel> list = new ArrayList<RSSChannel>();
            list.add(new RSSChannel("Хабрахабр | Захабренные | Отхабренные", "http://habrahabr.ru/rss/hubs/"));
            list.add(new RSSChannel("Хабрахабр | Лучшие за сутки", "http://habrahabr.ru/rss/best/"));
            return list;
        }*/
    };


        layout.setMargin(true);
        layout.setSizeUndefined();
        layout.setSpacing(true);
        setContent(layout);

        progressIndicator.setCaption("Filling channel list");

        channelSelection.setItemCaptionPropertyId("channelName");
        channelSelection.setNullSelectionAllowed(false);
        channelSelection.setRequired(true);
        channelSelection.setImmediate(true);

        channelFill.start();

        layout.addComponent(channelSelection);

        HorizontalLayout buttonRow = new HorizontalLayout();
        buttonRow.setSpacing(true);

        layout.addComponent(buttonRow);
        updateState(false);

        channelCaption = new TextField("Channel name");
        channelURL = new TextField("Channel URL");

        layout.addComponent(channelCaption);
        layout.addComponent(channelURL);

        layout.addComponent(new Label("<i>" + user.getFullName() + "</i>", ContentMode.HTML));

        createAddChannelButton();
        layout.addComponent(addChannelButton);
    }

    private void updateState(boolean enabled) {
        channelSelection.setEnabled(enabled);

        if (enabled && progressIndicator.getParent() != null) {
            layout.removeComponent(progressIndicator);
        } else if (!enabled && progressIndicator.getParent() == null) {
            layout.addComponent(progressIndicator, 0);
        }
    }

    /**
     * Fetch channels for current user
     * @return list of channels
     */
    private List<RSSChannel> fetchChannels(){
        List<RSSChannel> list = new ArrayList<RSSChannel>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            String dburl = "jdbc:mysql://localhost:3306/test";
            Connection conn = DriverManager.getConnection(dburl, "root", "unclefucka");
            //    Query the DB to get ResultSet back.
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT channelTitle, channelURL FROM channels");

            //    Loop through the ResultSet
            while (resultSet.next()) {
                ResultSetMetaData rsmd = resultSet.getMetaData();
                Reader channelTitleStream = resultSet.getCharacterStream(1);
                Reader channelURLStream = resultSet.getCharacterStream(2);

                char channelTitleChars[];
                char channelURLChars[];
                int channelTitleSize = rsmd.getColumnDisplaySize(1);
                int channelURLSize = rsmd.getColumnDisplaySize(2);
                channelTitleChars = new char[2*channelTitleSize];
                channelURLChars = new char[2*channelURLSize];

                channelTitleStream.read(channelTitleChars);
                channelURLStream.read(channelURLChars);
                String channelTitle = new String(channelTitleChars);
                String channelURL = new String(channelURLChars);

                list.add(new RSSChannel(channelTitle, channelURL));
            }
        } catch (Exception e) {
            Notification.show("ACTUNG!!1", Notification.Type.ERROR_MESSAGE);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            log.warn("ACHTUNG!!1", e);
        }
        finally {
            return list;
        }
    }


    private boolean addChannel(String caption, String url){
        if((!caption.isEmpty()) && url.startsWith("http://")){
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String dburl = "jdbc:mysql://localhost:3306/test";
                Connection conn = DriverManager.getConnection(dburl, "root", "unclefucka");
                //    Query the DB to get ResultSet back.
                PreparedStatement statement = conn.prepareStatement("INSERT INTO channels(channelTitle, channelURL, userId) VALUES(?, ?, ?)");
                statement.setString(1, caption);
                statement.setString(2, url);
                statement.setString(3, "" + user.getUserId());
                statement.executeUpdate();
                conn.close();
                return true;
            } catch (Exception e) {
                Notification.show("ACTUNG!!1", Notification.Type.ERROR_MESSAGE);
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                log.warn("ACHTUNG!!1", e);
                return false;
            }
        } else{
            Notification.show("Before adding a new channel, check that channel name is not empty and channel URL starts with http://", Notification.Type.ERROR_MESSAGE);
            return false;
        }
    }


    private void createAddChannelButton() {
        addChannelButton = new Button("Add channel");
        addChannelButton.setStyleName(BaseTheme.BUTTON_LINK);
        addChannelButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if(addChannel(channelCaption.getValue(), channelURL.getValue())){
                    Notification.show("Channel \"" +  channelCaption.getValue() + "\" with URL\"" + channelURL.getValue() + "\" added", Notification.Type.TRAY_NOTIFICATION);
                }
            }
        });
    }

}