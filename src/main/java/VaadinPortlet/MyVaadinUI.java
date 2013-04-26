package VaadinPortlet;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.model.User;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinPortlet;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.apache.commons.digester.rss.Channel;
import org.apache.commons.digester.rss.Item;
import org.apache.commons.digester.rss.RSSDigester;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.sql.*;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MyVaadinUI extends UI
{
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

    private VerticalLayout layout;

//    private Button changeChannelButton;
    private Button updateChannelButton;
    private Button addChannelButton;
//    private Button removeChannelButton;
    private Button manageChannelsButton;
    private Button databaseConnectButton;
    private List<RSSChannel> channels;
    private BeanItemContainer<RSSChannel> objects;
    private ComboBox channelSelection;
    private VerticalLayout RSSFeedContainer;
    private List<RSSChannel> channelList;
    private ManageChannelsWindow window;

    public User user;

    private static final Log log = LogFactoryUtil
            .getLog(ManageChannelsWindow.class);


    public User getUser(){
        return user;
    }

    @Override
    protected void init(VaadinRequest request) {
        VaadinPortlet.VaadinLiferayRequest liferayRequest = (VaadinPortlet.VaadinLiferayRequest) request;

        try {
            user = PortalUtil.getUser(liferayRequest.getRequest());
        } catch (PortalException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SystemException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }



        layout = new VerticalLayout();
        layout.setMargin(true);
        setContent(layout);

        RSSFeedContainer = new VerticalLayout();
        RSSFeedContainer.setCaption("RSS feed");

        createUpdateChannelButton();
//        createAddChannelButton();
//        createRemoveChannelButton();
        createManageChannelsButton();
        createUpdateChannelListButton();

//        layout.addComponent(changeChannelButton);
        layout.addComponent(updateChannelButton);
//        layout.addComponent(addChannelButton);
//        layout.addComponent(removeChannelButton);
        layout.addComponent(manageChannelsButton);
        layout.addComponent(databaseConnectButton);


        channels = new ArrayList<RSSChannel>();
//        channels.add(new RSSChannel("Хабрахабр | Захабренные | Отхабренные", "http://habrahabr.ru/rss/hubs/"));
//        channels.add(new RSSChannel("Хабрахабр | Лучшие за сутки", "http://habrahabr.ru/rss/best/"));

        objects = new BeanItemContainer(RSSChannel.class);

//        addChannel("Хабрахабр | Захабренные | Отхабренные", "http://habrahabr.ru/rss/hubs/");
//        addChannel("Хабрахабр | Лучшие за сутки", "http://habrahabr.ru/rss/best/");
//        objects.addAll(channels);
        channelList = fetchChannels();
        objects.addAll(channelList);

        channelSelection = new ComboBox("Channel", objects);
        channelSelection.setItemCaptionPropertyId("channelName");
        channelSelection.setNewItemsAllowed(false);
        channelSelection.setNullSelectionAllowed(false);
        channelSelection.setImmediate(true);
        channelSelection.removeItem("");
        channelSelection.setValue(channelSelection.getItemIds().iterator().next());

        layout.addComponent(channelSelection);

//        channelCaption = new TextField("Channel name");
//        channelURL = new TextField("Channel URL");

//        layout.addComponent(channelCaption);
//        layout.addComponent(channelURL);

//        parseRSS();

        layout.addComponent(RSSFeedContainer);
    }


    private void parseRSS(){
        RSSDigester digester = new RSSDigester();
        URL url = null;

        try {
            url = new URL(((RSSChannel)channelSelection.getValue()).getURL());

            HttpURLConnection httpSource = null;
            httpSource = (HttpURLConnection)url.openConnection();

            Channel channel = null;
            channel = (Channel)digester.parse(httpSource.getInputStream());

            if (channel == null) {
                throw new Exception("can't communicate with " + url);
            }

            Item rssItems[] = new Item[0];
            if (channel != null) {
                rssItems = channel.findItems();
            }

            RSSFeedContainer.removeAllComponents();

            for (Item rssItem : rssItems) {
                RSSFeedContainer.addComponent(new Label("<h2><b>" + rssItem.getTitle() + "</b></h2><br /><a href='" + rssItem.getLink() + "'>" + rssItem.getLink() + "</a><br />" + rssItem.getDescription(), ContentMode.HTML));
            }
        }
        catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     *  Add a new channel to the list
     */
    private boolean addChannel(String caption, String url){
        if((!caption.isEmpty()) && url.startsWith("http://")){
            objects.addBean(new RSSChannel(caption, url));
            return true;
        }
        else{
            Notification.show("Before adding a new channel, check that channel name is not empty and channel URL starts with http://", Notification.Type.ERROR_MESSAGE);
            return false;
        }
    }


    private void createUpdateChannelButton() {
        updateChannelButton = new Button("Update channel");
        updateChannelButton.setStyleName(BaseTheme.BUTTON_LINK);
        updateChannelButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                parseRSS();
                Notification.show("Channel " +  ((RSSChannel)channelSelection.getValue()).getChannelName() + " updated", Notification.Type.TRAY_NOTIFICATION);
            }
        });
    }


    private void createManageChannelsButton() {
        manageChannelsButton = new Button("Manage my channels");
        manageChannelsButton.setStyleName(BaseTheme.BUTTON_LINK);
        manageChannelsButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                window = new ManageChannelsWindow();
                window.addCloseListener(new Window.CloseListener(){
                    @Override
                    public void windowClose(Window.CloseEvent e) {
                        removeWindow(window);
                        objects.removeAllItems();
                        objects.addAll(fetchChannels());
                    }

                });
                addWindow(window);
            }
        });
    }


    private void createUpdateChannelListButton() {
        databaseConnectButton = new Button("Fetch my channels from DB");
        databaseConnectButton.setStyleName(BaseTheme.BUTTON_LINK);
        databaseConnectButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                objects.removeAllItems();
                channelList = fetchChannels();
                objects.addAll(channelList);
            }
        });
    }



    private List<RSSChannel> fetchChannels(){
        List<RSSChannel> list = new ArrayList<RSSChannel>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            String dburl = "jdbc:mysql://localhost:3306/test";
            Connection conn = DriverManager.getConnection(dburl, "root", "unclefucka");

            //    Query the DB to get ResultSet back.
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT channelTitle, channelURL FROM channels WHERE userId=" + user.getUserId());

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
            conn.close();
        } catch (Exception e) {
            Notification.show("ACTUNG!!1", Notification.Type.ERROR_MESSAGE);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            log.warn("ACHTUNG!!1", e);
        }
        finally {
            return list;
        }
    }


    /*private void createAddChannelButton() {
        addChannelButton = new Button("Add channel");
        addChannelButton.setStyleName(BaseTheme.BUTTON_LINK);
        addChannelButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if(addChannel(channelCaption.getValue(), channelURL.getValue())){
                    Notification.show("Channel \"" +  channelCaption.getValue() + "\" with URL\"" + channelURL.getValue() + "\" added", Notification.Type.TRAY_NOTIFICATION);
                }
            }
        });
    }*/


    /*private void createRemoveChannelButton() {
        removeChannelButton = new Button("Remove channel");
        removeChannelButton.setStyleName(BaseTheme.BUTTON_LINK);
        removeChannelButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                Notification.show("Channel " +  ((RSSChannel)channelSelection.getValue()).getChannelName() + " removed", Notification.Type.TRAY_NOTIFICATION);
                channelSelection.removeItem(channelSelection.getItemIds().iterator().next());
                channelSelection.setValue(channelSelection.getItemIds().iterator().next());
            }
        });
    }*/
}
