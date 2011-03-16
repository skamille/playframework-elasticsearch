package play.modules.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import play.Logger;
import play.Play;
import play.PlayPlugin;

// TODO: Auto-generated Javadoc
/**
 * The Class ElasticSearchPlugin.
 */
public class ElasticSearchPlugin extends PlayPlugin {

	/** The _session. */
	private ThreadLocal<Client> _session = new ThreadLocal<Client>();

	/** The started. */
	public static boolean started = false;

	/**
	 * Client.
	 * 
	 * @return the client
	 */
	public Client client() {
		return _session.get();
	}

	/**
	 * Elastic Search Start.
	 * 
	 * @see play.PlayPlugin#onApplicationStart()
	 */
	@Override
	public void onApplicationStart() {
		// Make sure it doesn't get started more than once
		if (_session.get() != null || started) {
			Logger.info("Elastic Search Started Already!");
			return;
		}

		// Start Node Builder
		NodeBuilder nb = nodeBuilder();

		// Check Local Mode
		boolean localMode = false;
		if (!Play.configuration.containsKey("elasticsearch.local")) {
			localMode = Boolean.getBoolean(Play.configuration
					.getProperty("elasticsearch.local"));
			nb = nb.local(localMode);
		}

		// Check Client Mode
		boolean clientMode = false;
		if (!Play.configuration.containsKey("elasticsearch.client")) {
			clientMode = Boolean.getBoolean(Play.configuration
					.getProperty("elasticsearch.client"));
			nb = nb.client(clientMode);
		}

		// Default to Local Mode
		if (localMode == false && clientMode == false) {
			localMode = true;
			nb = nb.local(localMode);
		}

		// Log Debug
		if (localMode) {
			Logger.info("Starting Play! Elastic Search in Local Mode");
		} else {
			Logger.info("Starting Play! Elastic Search in Client Mode");
		}

		// Mark as Started
		started = true;
		Node node = nb.node();
		Client client = node.client();

		// Start Indexes
		try {
			// Start Indexes
			String models = Play.configuration.getProperty("elasticsearch.models", "");
			String[] classes = StringUtils.split(models, ",");
			for ( String clazz : classes ) {
				Logger.info("Start Index for Class: %s", clazz);
				ElasticSearchAdapter.startIndex(client, Class.forName(clazz));
			}

		} catch (Throwable t) {
			Logger.warn(ExceptionUtil.getStackTrace(t));
			throw new RuntimeException(t);
		}

		// Bind Client to Thread Local
		_session.set(client);

	}

}