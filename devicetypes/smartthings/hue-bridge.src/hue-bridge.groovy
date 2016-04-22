/**
 *  Hue Bridge
 *
 *  Author: SmartThings
 */
// for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Hue Bridge", namespace: "smartthings", author: "SmartThings") {
		attribute "serialNumber", "string"
		attribute "networkAddress", "string"
        // Used to indicate if bridge is reachable or not, i.e. is the bridge connected to the network
        // Possible values "Online" or "Offline"
		attribute "status", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
     	multiAttributeTile(name:"rich-control"){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "Offline", label: '${currentValue}', action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#ffffff"
	            attributeState "Online", label: '${currentValue}', action: "", icon: "st.Lighting.light99-hue", backgroundColor: "#79b821"
			}
			}
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 2, width: 6, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}'
		}
		valueTile("networkAddress", "device.networkAddress", decoration: "flat", height: 2, width: 6, inactiveLabel: false) {
			state "default", label:'IP: ${currentValue}', height: 1, width: 2, inactiveLabel: false
		}

		main (["rich-control"])
		details(["rich-control", "serialNumber", "networkAddress"])
	}
}

// parse events into attributes
def parse(description) {
	log.debug "Parsing '${description}'"
	def results = []
	def result = parent.parse(this, description)
	if (result instanceof physicalgraph.device.HubAction){
		log.trace "HUE BRIDGE HubAction received -- DOES THIS EVER HAPPEN?"
		results << result
	} else if (description == "updated") {
		//do nothing
		log.trace "HUE BRIDGE was updated"
	} else {
		def map = description
		if (description instanceof String)  {
			map = stringToMap(description)
		}
		if (map?.name && map?.value) {
			log.trace "HUE BRIDGE, GENERATING EVENT: $map.name: $map.value"
			results << createEvent(name: "${map.name}", value: "${map.value}")
		} else {
        	log.trace "Parsing description"
			def msg = parseLanMessage(description)
			if (msg.body) {
				def contentType = msg.headers["Content-Type"]
				if (contentType?.contains("json")) {
					def bulbs = new groovy.json.JsonSlurper().parseText(msg.body)
					if (bulbs.state) {
						log.info "Bridge response: $msg.body"
					} else {
						// Sending Bulbs List to parent"
                        if (parent.state.inBulbDiscovery)
                        	log.info parent.bulbListHandler(device.hub.id, msg.body)
					}
				}
				else if (contentType?.contains("xml")) {
					log.debug "HUE BRIDGE ALREADY PRESENT"
                    parent.hubVerification(device.hub.id, msg.body)
				}
			}
		}
	}
	results
}
