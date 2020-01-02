/**
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *
 */
 
 // This is a BETA version of this software.
 // It is very close to being done but it does not yet have the spit and polish that I prefer before I release something.
 // However I'm going to be gone for a while and I would like to release it to those that are interested in trying it out.

metadata {
		definition (name: "Tasmota Plug with Power Monitoring for ST Classic V0.91", namespace: "garyjmilne", author: "Gary J. Milne")  {
		capability "Switch"
        capability "Configuration"
        capability "Voltage Measurement"
		capability "Power Meter"
        capability "Outlet"
        capability "Refresh"

		command "updateDeviceNetworkID"
        command "poll"
        command "sync"
        command "reset"
        command "timezoneLocal"
        command "timezoneUTC"
        command "moreinfo"
        command "power"
        command "usage"
        command "time"
        command "resetEnergy"
        command "refreshWiFi"
        
        attribute "Local", "string"				//Local time as reported by the plug, required for accurate power stats.
        attribute "UTC", "string"				//UTC time as reported by the plug
        attribute "Uptime", "string"			//Uptime of the plug
        attribute "Timezone", "string"			//Timezone of the plug
        attribute "StartupUTC", "string"		//Absolute time of startup
        attribute "TasmotaVersion", "string"	//Firmware version of the Tasmota
        attribute "Hostname", "string"			//Hostname of the Tasmota
        attribute "TotalStartTime", "string"	//Time at which energy totals are effective from.
        attribute "TotalPower", "number"		//Since Total Start Time
        attribute "TodayPower", "number"		//Total power used today
        attribute "YesterdayPower", "number"	//Total power used yesterday
        attribute "ApparentPower", "number"			
        attribute "ReactivePower", "number"			
        attribute "PowerFactor", "number"			
        attribute "Watts", "number"				//Present watts	
        attribute "Voltage", "number"			//Present Voltage
        attribute "Current", "number"			//Present Amps
        attribute "LastUpdate", "string"		//Time when stats were last retreived successfully
        attribute "OperatingState", "string"
        
        attribute "Message", "string"
        attribute "LastDeviceStandby", "number"  //The last time the downstream device was considered to be in standby mode
        attribute "LastDeviceOn", "number"  //The last time the downstream device was considered to be in On mode
		attribute "LastDeviceOff", "number"  //The last time the downstream device was considered to be in Off mode  
        attribute "stateDuration", "number"  //Time in seconds since the device entered the current state
    }
    
    simulator {
	}
    
    preferences {
    	section("Configure the Inputs"){
			input name: "destIp", type: "text", title: "IP", description: "The device IP", defaultValue: "192.168.0.X", required:true, displayDuringSetup: true
    		input name: "destPort", type: "text", title: "Port", description: "The webserver port.", defaultValue: "80", required:false, displayDuringSetup: true
          	input name: "username", type: "text", title: "Username", description: "Username (if configured)", required: false, displayDuringSetup: true
          	input name: "password", type: "password", title: "Password", description: "Password (if configured)", required: false, displayDuringSetup: true
            input name: "frequency", type: "number", title: "Auto sync in X minutes", description: "Enter 1, 5, 10, 15 or 30 minutes.", defaultValue: "1", required:false, displayDuringSetup: false
            input name: "tzoffset", type: "text", title: "The Plug Timezone Offset", description: "Range +12:00 to -12:00 (default:00:00)", defaultValue: "+00:00", required: false, displayDuringSetup: true
            input name: "standbyThreshold", type: "number", title: "Standby Power Threshold", description: "Device on standby when power between 0 and threshold (default:2)", defaultValue: "2", required: false, displayDuringSetup: true
            input name: "electricalCost",  type: "number", title: "Electrical Cost  per kWh", description: "The charge in dollars per kWh for electricity in your home", defaultValue: "0.12", required: false, displayDuringSetup: true
            input name: "currencySymbol",  type: "text", title: "Currency Symbol", description: "Currency symbol i.e. " + "\$" + " (dollar) £ (pound) ¥ (yen). ", defaultValue: "\$", required: false, displayDuringSetup: true
           	}
	}

	tiles(scale: 2){
    
         standardTile("switch", "device.switch", inactiveLabel: false, canChangeIcon: true, width: 3, height: 3 ) {
             state "on", label:"On", action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
             state "off", label:"Off", action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
             state "turningOn", label:"Turning On", action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
             state "turningOff", label:"Turning Off", action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
		}  
        
        standardTile("operatingState", "device.OperatingState", inactiveLabel: false, decoration: "flat", width: 3, height: 2 ) {
        	state "off", label:"Device Off", action:"", defaultState: true, icon:"https://img.icons8.com/color/96/000000/tv-off.png" //"https://img.icons8.com/color/96/000000/light-off.png"
            state "standby", label:"Standby", action:"", defaultState: false, icon:"https://img.icons8.com/color/96/000000/tv.png"
            state "on", label:"Device On", action:"", defaultState: false, icon:"https://img.icons8.com/color/96/000000/reality-stars.png" // //"https://img.icons8.com/color/96/000000/light-automation.png"
		}
        
        valueTile("message", "device.Message", width: 3, height: 1) {state "default", label:'${currentValue}'}
        valueTile("watts", "device.Watts", width: 2, height: 1) {state "default", label:'${currentValue} W'}
        valueTile("voltage", "device.Voltage", width: 2, height: 1) {state "default", label:'${currentValue} V'}
        valueTile("current", "device.Current", width: 2, height: 1) {state "default", label:'${currentValue} A'}
        
        standardTile("info", "device.info", inactiveLabel: false, decoration: "flat", width: 2, height: 2 ) {
             state "Usage1", label: '${Name}', action:"moreinfo", icon:"https://img.icons8.com/color/96/000000/bar-chart.png" 
             state "Usage2", label: '${Name}', action:"moreinfo", icon:"https://img.icons8.com/color/96/000000/us-dollar--v1.png"
             state "Power", label: '${Name}', action:"moreinfo", icon:"https://img.icons8.com/color/96/000000/energy-meter.png"
             state "Device", label: '${Name}', action:"moreinfo", icon:"https://img.icons8.com/color/96/000000/clock--v1.png"
             state "Plug", label: '${Name}', action:"moreinfo", icon:"https://img.icons8.com/color/96/000000/electrical.png"
		}
        
        valueTile("info1", "device.info1", inactiveLabel: false, decoration: "flat", width: 4, height: 2) {
			state "", label: '${currentValue}'
		}
        
        standardTile("sync", "device.sync", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "idle", label:'Sync', action:"sync", defaultState: true, icon:"https://img.icons8.com/color/96/000000/replace.png"
            state "running", label:'Sync', action:"", defaultState: false, icon:"https://img.icons8.com/color/96/000000/synchronize--v1.png"
		}
        
        standardTile("status", "device.status", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "idle", label:'Status Idle', action:"", defaultState: true//, icon:"https://img.icons8.com/color/96/000000/sleep.png"
            state "send", label:'Sending', action:"", defaultState: false, icon:"https://img.icons8.com/color/96/000000/outgoing-data.png"
            state "wait", label:'Waiting', action:"", defaultState: false, icon:"https://img.icons8.com/color/96/000000/time.png"
            state "receive", label:'Receiving', action:"", defaultState: false, icon:"https://img.icons8.com/color/96/000000/incoming-data.png"
            state "success", label:'Success', action:"", defaultState: false, icon:"https://img.icons8.com/color/96/000000/checkmark.png"
            state "fail", label:'Fail', action:"", defaultState: false, icon:"https://img.icons8.com/color/96/000000/error.png"
		}
        
        standardTile("multi", "device.multi", inactiveLabel: false, decoration: "flat", width: 2, height: 2 ) {
        	 state "disabled", label: '${Name}', action: "", defaultState: true, icon: "https://img.icons8.com/color/96/000000/cancel-2--v1.png"
             state "Reset Energy", label: '${Name}', action:"resetEnergy", icon: "https://img.icons8.com/color/96/000000/clear-symbol.png"
             state "Local", label: "Using UTC", action:"timezoneLocal", icon: "https://img.icons8.com/color/96/000000/globe-earth.png"
             state "UTC", label: "Using Local", action:"timezoneUTC", icon: "https://img.icons8.com/color/96/000000/order-delivered.png"
		}
        
        standardTile("wifi", "device.wifi", inactiveLabel: false, decoration: "flat", width: 2, height: 2 ) {
        	 state "Unknown", label: '${currentValue}', action: "refreshWiFi", defaultState: true, icon: "https://img.icons8.com/color/96/000000/question-mark.png"
             state "No", label: '${currentValue}', action:"refreshWiFi", icon: "https://img.icons8.com/color/96/000000/no-connection.png"
             state "Low", label: '${currentValue}', action:"refreshWiFi", icon: "https://img.icons8.com/color/96/000000/low-connection.png"
             state "Medium", label: '${currentValue}', action:"refreshWiFi", icon: "https://img.icons8.com/color/96/000000/medium-connection.png"
             state "High", label: '${currentValue}', action:"refreshWiFi", icon: "https://img.icons8.com/color/96/000000/high-connection.png"     
		}
        
        standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 2, height: 2 ) {
			state "", label: 'Reset UI', action:"reset", icon: "https://img.icons8.com/color/96/000000/recurring-appointment--v2.png"
		}
        
        standardTile("dni", "device.dni", inactiveLabel: false, decoration: "flat", width: 3, height: 2 ) {
			state "", label:'DNI:${currentValue}', action:"updateDeviceNetworkID", defaultState: true, icon:"https://img.icons8.com/color/96/000000/ip-address.png"
       	}
        
        standardTile("icons", "device.icons", inactiveLabel: false, decoration: "flat", width: 3, height: 2 ) {
			state "icon1", label:'Icon Credits: Icons8\nwww.icons8.com', action:"icons2", defaultState: false, icon:"https://img.icons8.com/color/96/000000/icons8-new-logo.png"
		}
        
        //These remaining tiles are never displayed.  They are effectively used as global variables which are otherwise not permitted.
        valueTile("command", "device.command", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			 state "", label: '${currentValue}'
		}
        
        valueTile("commandvalue", "device.commandvalue", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "", label: '${currentValue}'
		}
        
        valueTile("commandtime", "device.commandtime", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "", label: '${currentValue}'
		}
        
        valueTile("commandflag", "device.commandflag", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			 state "", label: '${currentValue}'
		}

		main(["switch"])
        details(["switch", "operatingState", "message", "watts", "voltage", "current", "info", "info1", "sync", "status", "multi", "wifi", "reset", "dni", "icons" ])
	}
}

//*********************************************************************************************************************************************************************
// Start of user modifiable functions

//Function to selectively log activity based on varios logging levels. Normal runtime configuration is threshold = 0
//Loglevels are cumulative: -1 All errors, 0 = All user actions plus status, 1 = Entering\Exiting modules with parameters, 2 = Key variables, 3 = Extended debugging info, 4 = Extreme includes inner loops
private log(name, message, loglevel){
	
    //This is a quick way to filter out messages based on loglevel
	def threshold = 0
    if (loglevel > threshold) {return}
    
    //This is a quick way to filter out messages from a given function
    def filterlist = "listofnamestofilter: function1, anothername"
    if (filterlist.contains(name) == true) {return}

    def icon1 = " "
    def icon2 = "🏃"
    def indent = ".."
    if (loglevel == -1) icon1 = "🛑"
    if (loglevel == 0) icon1 = "0️⃣"
    if (loglevel == 1) icon1 = "1️⃣"
    if (loglevel == 2) icon1 = "2️⃣"
    if (loglevel == 3) icon1 = "3️⃣"
     
    switch(name) {                 
        case "parse":
        indent = "...."
        break;

        case "checkResponse":
        indent = "........"
        icon2 = "🏁"
        break;
    }
    
    //Keyword search and icon replacement. Obviously icon2 may get overwritten so order is important.
    if (name.toString().toUpperCase().contains(" ACTION")==true ) icon2 = "🚀"
    if (name.toString().toUpperCase().contains("CALLTASMOTA")==true ) icon2 = "☎️"
    if (message.toString().toUpperCase().contains("TIMEZONE")==true ) icon2 = "🕒"
    if (message.toString().toUpperCase().contains("TURN ON")==true ) icon2 = "🟡"
    if (message.toString().toUpperCase().contains("TURN OFF")==true ) icon2 = "⚪"
    if (message.toString().toUpperCase().contains("SEND")==true ) icon2 = "⇢🔌"
    if (message.toString().toUpperCase().contains("RECEIVE")==true ) icon2 = "⇠🔌"
    if (message.toString().toUpperCase().contains("JSON")==true ) icon2 = "🔢"
    if (message.toString().toUpperCase().contains("SYNC")==true ) icon2 = "🔄"
    if (message.toString().toUpperCase().contains("BLOCKED")==true ) icon2 = "👎"
    if (message.toString().toUpperCase().contains("ALLOWED")==true ) icon2 = "👍"
    if (name.toString().toUpperCase().contains("POLL")==true ) icon2 = "🔂"
    if (message.toString().toUpperCase().contains("EXIT")==true ) icon2 = "💨"

    log.debug (icon2 + " " + icon1 +  "${indent}${name}: " + message)       
}

//Formats messages for display on the screen
private message(message){
	
    if (message == null) message = ""
    //Most messages that come through are the current color which has no associated text.
    def icon1 = ""
    
    //Keyword search and icon replacement. Obviously icon2 may get overwritten so order is important.
    if (message.toString().toUpperCase().contains("SYNC ")==true ) icon1 = "🔄"
    if (message.toString().toUpperCase().contains("SUCCESS")==true ) icon1 = "✅"
    if (message.toString().toUpperCase().contains("POLL")==true ) icon1 = "🔂"
    if (message.toString().toUpperCase().contains("WAIT")==true ) icon1 = "⏳"
    if (message.toString().toUpperCase().contains("FAIL")==true ) icon1 = "❌"
    if (message.toString().toUpperCase().contains("SYNC SUCCESS")==true ) icon1 = "📜"
    if (message.toString().toUpperCase().contains("INVALID")==true ) icon1 = "⚠️"
    if (message.toString().toUpperCase().contains("BUSY")==true ) icon1 = "⚠️"
    sendEvent(name:"Message", value: icon1 + message  )    
}

// End of user modifiable functions
//*********************************************************************************************************************************************************************

//*********************************************************************************************************************************************************************
// Start of standard functions
def installed(){
	log ("Installed with settings: ${settings}", 0)
}

def updated(){
	log ("Update", "Settings: ${settings}", 0)
	initialize()
}

def uninstalled() {
	log ("Uninstall", "Device uninstalled", 0)
}

def initialize(){
	log("Initialize", "Device initialized", 0)
    //Cancel any existing scheduled tasks for this device
    unschedule("${device}")
	//Make sure we are using the right address
    updateDeviceNetworkID()
    
    //Populate an initial time so we don't get null
    long timeStart = now()
    sendEvent(name: "commandtime", value: timeStart, displayed:false)
    
    //Sometime the first attempt to change the DNI does not work so we will schedule a second attempt 10 seconds from now.
    runIn(10,updateDeviceNetworkID)
    
	//Test to make sure the entered frequency is in range
    switch(settings.frequency) { 
        case 1: runEvery1Minute(poll) ; break;
        case 5: runEvery5Minutes(poll) ; break;
        case 10: runEvery10Minutes(poll) ; break;
        case 15: runEvery15Minutes(poll) ; break;
        default: runEvery1Minute(poll) ; break;
    } 
   
   	//These need to be populated with initial values or they will return null
    if ( device.currentValue("status") == null ) sendEvent(name:"status", value: "idle")
	if ( device.currentValue("sync") == null ) sendEvent(name:"sync", value: "enabled")
	if ( device.currentValue("Message") == null ) sendEvent(name:"Message", value: "")
    if ( device.currentValue("moreinfo") == null ) sendEvent(name:"moreinfo", value: "Today Power")
    if ( device.currentValue("operatingState") == null ) sendEvent(name: "operatingState", value: "off", isStateChange:true)
    if ( device.currentValue("LastDeviceStandby") == null ) sendEvent(name: "LastDeviceStandby", value: 0, isStateChange:true)
	if ( device.currentValue("LastDeviceOn") == null ) sendEvent(name: "LastDeviceOn", value: 0, isStateChange:true)
	if ( device.currentValue("LastDeviceOff") == null ) sendEvent(name: "LastDeviceOff", value: 0, isStateChange:true)
	if ( device.currentValue("stateDuration") == null ) sendEvent(name: "stateDuration", value: 0, isStateChange:true) 
}
// End of standard functions
//*********************************************************************************************************************************************************************

//*********************************************************************************************************************************************************************
// Start of functions called from UI actions

//Turns the switch on
def on() {
	log("Action", "Turn on switch", 0)
	if ( isSystemIdle() == true ){	
        callTasmota("POWER", "On")
        if (checkResponse() == true) {
        	updatePowerState("on")
			refreshinfo()
            }
        else{
            //Lets make sure it does not get stuck in one of the transition states
        	if ( device.currentValue("switch") != "off" ) sendEvent(name:"switch", value: "off", isStateChange: true)
            updatePowerState("off")
        	}
    	}
	}

//Turns the switch off
def off() {
	log("Action", "Turn off switch", 0)
	if ( isSystemIdle() == true ){	
    	callTasmota("POWER", "Off")
        if (checkResponse() == true) {
        	updatePowerState("off")
            setOperatingState()
			refreshinfo()
            }
        else{
        	//Lets make sure it does not get stuck in one of the transition states
        	if ( device.currentValue("switch") != "on" ) sendEvent(name:"switch", value: "on", isStateChange: true)
        	updatePowerState("on")
            setOperatingState()
        	}
    	}
	}
    
//Updates the switch state, operating state and statistics. Especially to reflect an On to Off state change where many stats go to zero.
def updatePowerState(state){
	log ("updatePowerState", "Value:" + state, 1)
	switch(state) { 
		case ["off"]:                
            if (device.currentValue("switch") != "off") {
                sendEvent(name:"switch", value: "off", isStateChange: true, displayed:true)
                //If the power goes off to the plug the device must also be off.
                sendEvent(name:"stateDuration", value: 0)
                sendEvent(name:"LastDeviceOff", value: now(), isStateChange: true, displayed: false)
                }

			//We know the device operatingState must be off if the plug is off.
            if (device.currentValue("operatingState") != "off") {
                sendEvent(name: "operatingState", value: "off", isStateChange:true, displayed:true)
                }

            sendEvent(name:"Watts", value: 0, isStateChange: true, displayed:false)
            sendEvent(name:"Amps", value: 0, isStateChange: true, displayed:false)
            sendEvent(name:"ApparentPower", value: 0, isStateChange: true, displayed:false)  
            sendEvent(name:"ReactivePower", value: 0, isStateChange: true, displayed:false)   
            sendEvent(name:"PowerFactor", value: 0, isStateChange: true, displayed:false)
            break

        case ["on"]: 
            if (device.currentValue("switch") != "on"){
                sendEvent(name:"switch", value: "on", isStateChange: true, displayed: true)
                //A state change so reset the timer
                sendEvent(name:"stateDuration", value: 0)
                }
            //It seems like we should call setOperatingState() here but there is a delay in the power statistics being generated at the plug so it is pointless
            //The change to the OperatingState needs to get picked up on the next cycle.
            break
        }
}

//Sets the operating state of the downstream device based on power consumption. Also updates the On\Off\Standby times for downstream device.
def setOperatingState(){
	def float standbyThreshold = settings.standbyThreshold
    def float Watts
    
	if (device.currentValue("Watts") == null) {
    		Watts = 0
        }
    else
    	{
    		Watts = device.currentValue("Watts")
    	}
    
    log("setOperatingState", "Current Watts are:(${Watts}) .... StandbyThreshold is: ${standbyThreshold}" + " ...Operating State is:" + device.currentValue("operatingState"), 0)
    if (Watts == 0 ) {
    	if (device.currentValue("operatingState") != "off"){
        	sendEvent(name:"operatingState", value: "off", isStateChange: true, displayed: true)
            }
		}
    
   	if ( (Watts > 0) & (Watts <= standbyThreshold)) {
    	if (device.currentValue("operatingState") != "standby") {
        	sendEvent(name:"operatingState", value: "standby", isStateChange:true, displayed: true)
            sendEvent(name:"LastDeviceStandby", value: now(), isStateChange: true, displayed: false)
			}
		}
    
    if (Watts > standbyThreshold) {
    	if (device.currentValue("operatingState") != "on") {
        	sendEvent(name: "operatingState", value: "on", isStateChange: true, displayed: true)
            sendEvent(name:"LastDeviceOn", value: now(), isStateChange: true, displayed: false)
        	}
		}
        
	//Now that we have updated the operatingState if neccessary, we can now update the timers.
	def long since 
  	switch(device.currentValue("operatingState")) { 
        case ["off"]:                
			since = device.currentValue("LastDeviceOff")
        	break

        case ["standby"]: 
            since = device.currentValue("LastDeviceStandby")
            break

        case ["on"]: 
            since = device.currentValue("LastDeviceOn")
            break
        }
	sendEvent(name:"stateDuration", value: timeSince(since), isStateChange: true, displayed: true)
}


//Section End - All of the above functions are on the top row of the device handler.
//************************************************************************************************************************

//************************************************************************************************************************
//This section of functions relates to the multipurpose "Info" tile which displays various information on the "Info2" tile.
//The "multi" tile is turned off and on to display functions that relate to the present state of info being displayed. ie resetting energy total, resetting timezone etc.
def moreinfo(){
    //This advances the tile to the next state and updates the info.
    switch(device.currentValue("info")) { 
        case ["Usage1"]:                
        usage2()
        break

        case ["Usage2"]:                
        power()
        break

        case ["Power"]: 
        device()
        break;

        case ["Device"]: 
        plug()
        break;

        case ["Plug"]: 
        usage1()
        break;

        default:
        power()
        break
    }
}

def refreshinfo(){
    //This refreshes the existing information on the tile.
    switch(device.currentValue("info")) { 
        case ["Usage1"]:                
        usage1()
        break

        case ["Usage2"]:                
        usage2()
        break

        case ["Power"]: 
        power()
        break;

        case ["Device"]: 
        device()
        break;

        case ["Plug"]: 
        plug()
        break;

        default:
        usage1()
        break
    }
}

//Displays yesterdays and todays power usage.
def usage1(){
	log ("usage1", "Execute usage1", 1)
    def msg = "Today: " + device.currentValue("TodayPower") + " kWh\n"
    msg = msg + "Yesterday: " + device.currentValue("YesterdayPower") + " kWh"
    sendEvent(name:"info1", value: msg, isStateChange: true, displayed: false)
	sendEvent(name:"info", value: "Usage1", isStateChange: true, displayed: false)
    sendEvent(name:"multi", value: "disabled", isStateChange: true, displayed: false)
}

def usage2(){
	log ("usage2", "Execute usage2", 1)
    def totalStartTime = device.currentValue("TotalStartTime")
    def usage = device.currentValue("TotalPower")
    def float cost = usage * settings.electricalCost
    totalStartTime = totalStartTime.substring(0,10)
    def msg = "Total: " + usage + " kWh\n"
    msg = msg + "(since: " +  totalStartTime + ")\n"
    msg = msg + "Cost: " + settings.currencySymbol + cost.round(2)
    sendEvent(name:"info1", value: msg, isStateChange: true, displayed: false)
	sendEvent(name:"info", value: "Usage2", isStateChange: true, displayed: false)
    sendEvent(name:"multi", value: "Reset Energy", isStateChange: true, displayed: false)
}

def power(){
	log ("power", "Execute power", 1)
    def msg = "Apparent Power: " + device.currentValue("ApparentPower") + " VA\n"
    msg = msg + "Reactive Power: " + device.currentValue("ReactivePower") + " VAr\n"
    msg = msg + "Power Factor: " + device.currentValue("PowerFactor")
    sendEvent(name:"info1", value: msg, isStateChange: true, displayed: false)
	sendEvent(name:"info", value: "Power", isStateChange: true, displayed: false)
    sendEvent(name:"multi", value: "disabled", isStateChange: true, displayed: false)
}

def device(){
	log ("device", "Execute device", 1)
	def myMap
    def msg
    
	myMap = formatTime(device.currentValue("LastDeviceOn"))
    if (myMap.timeLong == 0) msg = "On: --" + "\n"
    else { msg = "On: " + myMap.timeDate + "@" + myMap.timeTime + "\n" }
    
    myMap = formatTime(device.currentValue("LastDeviceStandby"))
    if (myMap.timeLong == 0) msg = msg + "Standby: No data" + "\n"
    else { msg = msg + "Standby: " + myMap.timeDate + "@" + myMap.timeTime + "\n" }
    
    myMap = formatTime(device.currentValue("LastDeviceOff"))
    if (myMap.timeLong == 0) msg = msg + "Off: No data" + "\n"
    else { msg = msg + "Off: " + myMap.timeDate + "@" + myMap.timeTime }
   
   	//msg = msg + "Duration: " + device.currentValue("stateDuration") + "(secs)"
    sendEvent(name:"info1", value: msg, isStateChange: true, displayed: false)
	sendEvent(name:"info", value: "Device", isStateChange: true, displayed: false)
    sendEvent(name:"multi", value: "disabled", isStateChange: true, displayed: false)
}

def plug(){
	log ("plug", "Execute plug", 1)
	def myMap
    //Use the appropriate time
    if ( usingLocalTime() == true) {
    	myMap = tasmotaTimeToMap(device.currentValue("Local"))
        }
    else {
    	myMap = tasmotaTimeToMap(device.currentValue("UTC"))
    }
    
    def msg = "Time: " + myMap.Month + " " + myMap.Date + "@" + myMap.Time + "\n"
    msg = msg + "Timezone: " + device.currentValue("Timezone") + "\n"
    msg = msg + "Uptime: " + device.currentValue("Uptime")  + "\n"
    //msg = msg + "StartupUTC: " + device.currentValue("StartupUTC")
    sendEvent(name:"info1", value: msg, isStateChange: true, displayed: false)
	sendEvent(name:"info", value: "Plug", isStateChange: true, displayed: false)
    if ( usingLocalTime() == true) {
    	log ("plug", "usingLocalTime: true", 1)
    	sendEvent(name:"multi", value: "UTC", isStateChange: true, displayed: true)
        }
    else {
    	log ("plug", "usingLocalTime: false", 1)
    	sendEvent(name:"multi", value: "Local", isStateChange: true, displayed: true)
    }
}



//This is the end of the section for functions that relate to the multipurpose "Info" tile which displays various information on the "Info2" tile.
//**************************************************************************************************************************************************

//**************************************************************************************************************************************************
//This is the start of the section for functions called from the use of the "multi" tile

//Sets the TotalEnergy stat back to 0
def resetEnergy(){
	log("Action", "Reset Total Energy", 0)
    def date = new Date()
    
	if ( isSystemIdle() == true ){	
        callTasmota("ENERGYRESET3", "0")
        if (checkResponse() == true) {
        	sendEvent(name: "TotalPower", value: "0", isStateChange: true, displayed: true)
            //Reset the TotalStartTime, which is just a date, to today. Will get overwritten on the next refresh.
            sendEvent(name: "TotalStartTime", value: date.format("yyyy-MM-dd"), isStateChange: true, displayed: true)
			refreshinfo()
		}
	}
}

//Determine if we are using local or UCT time.
def usingLocalTime(){
	def String TasmotaTimezone = device.currentValue("Timezone")
	if (TasmotaTimezone.contains("00:00") == true ) {
        return false
        }
    else {
        return true
    }
}

//Sets the timezone to Local time. Best for more logical and accurate power stats.
def boolean timezoneLocal(){
    def tzoffset = settings.tzoffset
    log("timezoneLocal", "Setting timezone set to Local (" + tzoffset + ")", 0)
    
	if ( isSystemIdle() == true ){	
    	callTasmota("TIMEZONE", "${tzoffset}")
        if (checkResponse() == true) {
            //Trigger refreshinfo() to update the display with any changes
			refreshinfo()
            }
       }
   }

//Sets the timezone to UTC. 
def timezoneUTC(){
	log("timezoneUTC", "Setting timezone set to UTC (00:00)", 0)
    if ( isSystemIdle() == true ){	
    	callTasmota("TIMEZONE", "00:00")
        if (checkResponse() == true) {
            //Trigger refreshinfo() to update the display with any changes
            refreshinfo()
            }
       }
   }

//This is the end of the section for functions called from the use of the "multi" tile
//**************************************************************************************************************************************************


//**************************************************************************************************************************************************
//Sync the UI to the actual status of the device. The results come back to the parse function.
//This function is called from the button press and automatically via the polling method
def sync(){
	log("sync", "Starting Sync", 0)
	if ( isSystemIdle() == true ){  //Performs a check to see if another command is in progress already 
        def mytime = elapsedTime()
        if ((device.currentValue("commandflag") == "Complete" ) || (elapsedTime() > 10)){
            log ("sync", "Sync Running..", 1)
            sendEvent(name: "sync", value: "running", isStateChange: true, displayed: true)
            message("Sync Running")
            //Status 0 gets all of the stats but may be truncated by SmartThings. In which case we must rely on specific calls.
            callTasmota("STATUS", "0" )
            
            if (checkResponse() == true) {
            	//We had a successful sync
                mytime = elapsedTime()
                log ("sync", "Sync Success...${mytime} seconds", 0)
                message("Sync Success")
                //Trigger refreshinfo() to update the display with any changes
            	refreshinfo()
                } 
            else {
            	//We did not get a response as expected. Sync Failed
            	mytime = elapsedTime()
            	log ("sync", "Sync Failed...${mytime} seconds", -1)
                message("Sync Failed")
           		}
            sendEvent(name: "sync", value: "idle", isStateChange: true, displayed: true)
            }
	else{
		log ("Sync", "Not executed..command in progress: " + device.currentValue("commandflag"), 0)
		}
	}
    setOperatingState()
}

//Get the updated WiFi strength
def refreshWiFi(){
	if ( isSystemIdle() == true ) callTasmota("STATUS", "11")
}


//Reset the UI if it ever gets stuck - manually initiated
def reset (){
	log("Action", "Reset the UI", 0)
	//Populate an initial time so we don't get null
    long timeStart = now()
    sendEvent(name: "commandtime", value: timeStart, isStateChange: true, displayed: false)
    sendEvent(name:"status", value: "idle", isStateChange: true, displayed: false)
    sendEvent(name:"sync", value: "idle", isStateChange: true, displayed: true)
    sendEvent(name: "commandflag", value: "Complete", isStateChange: true, displayed: false)
    message("")
    
    sendEvent(name:"info", value: "Usage1", isStateChange: true, displayed: false)
    usage1()  
    }

//End of functions called by direct user action.
//******************************************************************************************************************



//Test to determine whether another operation is in progress. If we have two requests at the same time then there is no guarantee that they will be returned in the correct order.  Results could be problematic.
def isSystemIdle(){
	def mytime = elapsedTime()
    //If the command is flagged complete or it has been more than 10 seconds since the last command was launched 
    //then we can assume that we are no longer waiting for a response.
    log ("isSystemIdle", "Command: ${device.currentValue("command")}  Commandflag: ${device.currentValue("commandflag")}...ElapsedTime: ${mytime}", 2 )
    if ((device.currentValue("commandflag") == "Complete" ) || (elapsedTime() > 10)){
    	log ("isSystemIdle", "True - callTasmota() - Allowed", 0)
		return true
        }
   else {
   		log ("isSystemIdle", "False - callTasmota() - Blocked", 0)
   		message("System busy")
        return false
	}
}

// End of functions called from UI actions
//*********************************************************************************************************************************************************************

//*********************************************************************************************************************************************************************
// Start of Background task run by Smartthings - Is executed by the polling function which syncs the state of the bulb with the UI. The bulb being considered authoritative.
//Runs on a frequency determined by the user. It will synchronize the Smartthings values to those of the actual bulb.
def poll(){
	    log ("Poll", "Polling started.. ", 0)
        sendEvent(name: "sync", value: "running", isStateChange: true, displayed: true)
        sync()
        sendEvent(name: "sync", value: "idle", isStateChange: true, displayed: true)
	}
// End of Background task
//*********************************************************************************************************************************************************************

//*********************************************************************************************************************************************************************
//Start of main program section where most of the work gets done. There are 3 functions callTasmota, parse and checkResponse
//This function places a call to the Tasmota device using HTTP via a hubCommand. A successful call will result in an HTTP response which will go to the parse() function
//Method is either IP or MQTT. We force using IP mode for reading status when the device handler is otherwise in MQTT mode.
def callTasmota(action, receivedvalue){
	def value = receivedvalue.toString()
    log ("callTasmota", "Sending command: ${action} ${value}", 0)
    
    //Update the status to show that we are sending info to the device
	sendEvent(name:"status", value: "send")

    //Capture what we are doing so we can validate whether it executed successfully or not
    //We are essentially using value tiles as global variables.
    sendEvent(name: "command", value: "${action}", isStateChange: true, displayed: false)
    sendEvent(name: "commandvalue", value: "${value}", isStateChange: true, displayed: false)
    long timeStart = now()
    sendEvent(name: "commandtime", value: timeStart, isStateChange: true, displayed: false)
    sendEvent(name: "commandflag", value: "Processing", isStateChange: true, displayed: false)
    def path

    //Clean up the strings to make them compatible with HTML
    value = value?.replace(" ","%20") 
    value = value?.replace("#","%23") 
    value = value?.replace(";","%3B") 
    path = "/cm?user=${username}&password=${password}&cmnd=${action}%20${value}"
    
    log ("callTasmota", "Path: ${path}", 1)
    try {
            def hubAction = new physicalgraph.device.HubAction(
                method: "GET",
                path: path,
                headers: [HOST: "${settings.destIp}:${settings.destPort}"]
                )
            log ("callTasmota", "hubaction: ${hubAction}", 3)
            sendHubCommand(hubAction)
        }
        catch (Exception e) {
            log ("calltasmota", "Exception $e in $hubAction", -1)
        }
    log ("callTasmota","Exiting", 2)
}


//Any successful call made to the device will have a return value which come back to the parse function.
//The parse function either routes the received JSON information to parsedevice or parsemqtt for further processing.
def parse(response){
	def msg = parseLanMessage(response)
    log ("parse","Entering - JSON data is: ${msg.json}", 1)
    def tasPower
    def tasTimeZone
	
    //From time to time we will get spurious returns after a time out period which we can discard.
    if ((device.currentValue("commandflag") == "Complete" ) || (elapsedTime() > 10)) return   
    log ("parse", "Response received", 0)
    
    //Update the status to show we have received a response
	sendEvent(name:"status", value: "receive")
	
    //Get the command and value that was submitted to the callTasmota function
    def lastcommand = device.currentValue("command")
    def lastcommandvalue = device.currentValue("commandvalue")
    log("parse", "lastcommand: ${lastcommand} - lastcommandvalue: ${lastcommandvalue}", 1)
    
    if (msg?.json != null){ updateAttributes(msg.json) }

    switch(lastcommand.toUpperCase()) { 
        case ["POWER"]:
            if (msg?.json?.POWER !=null) {tasPower = msg?.json?.POWER}
            	else tasPower = "Unknown"
            log("parsedevice","POWER: ${tasPower}", 0)
            if (lastcommandvalue.toUpperCase() == tasPower){
                log ("parsedevice","Power state applied successfully", 0)
                sendEvent(name: "commandflag", value: "Complete", isStateChange: true, displayed: false)
                //We got the response we were looking for so we can actually change the state of the switch in the UI.
                sendEvent(name: "switch", value: lastcommandvalue, isStateChange: true, displayed: true)
            } 
            else log("parsedevice","Power state failed to apply", -1)
            break

        case ["TIMEZONE"]:
        	if (msg?.json?.Timezone !=null) tasTimeZone = msg?.json?.Timezone
            	else tasTimeZone = "Unknown"
            log("parsedevice","TIMEZONE: ${tasTimeZone}", 0)

            if ( tasTimeZone.toUpperCase().contains(lastcommandvalue)){
                log ("parsedevice","Time Zone applied successfully", 0)
                sendEvent(name: "commandflag", value: "Complete", isStateChange: true, displayed: false)
                //We got the response we were looking for so we can actually change the state of the switch in the UI.
                sendEvent(name: "Timezone", value: lastcommandvalue, isStateChange: true, displayed: true)
            } 
            else log("parsedevice","Time Zone failed to apply", -1)
            break

        case ["STATUS"]:
        	updateAttributes(msg)
            if (lastcommandvalue == "11"){
                if (msg?.json?.StatusSTS?.Wifi?.RSSI != null ){
                    def RSSI = msg?.json?.StatusSTS?.Wifi?.RSSI
                    log ("parsedevice", "WiFi RSSI: ${RSSI}", 0)
                    setWifi(RSSI)   
                    }
                else 
                    {
                    log ("parsedevice","Unable to read WiFi status", 0)
                    }
            	}
			break
		
        case ["STATE"]:
        	updateAttributes(msg)
			break
    }
    //Mark the transaction as complete
	log ("parsedevice","Setting device handler values", 1)
	sendEvent(name: "commandflag", value: "Complete", isStateChange: true, displayed: false)
	sendEvent(name: "sync", value: "enabled", isStateChange: true, displayed: false)
   	log ("parse","Exiting", 2)
   }


//Updates all of the device attributes with information received from parsedevice
//Not all information is updated each time depending on the original command
def updateAttributes(msg){
	//Status - First line returned
	if (msg?.json?.Status != null){
    	sendEvent(name: "FriendlyName", value: msg?.json?.Status?.FriendlyName, isStateChange: true, displayed: false)
        //Update the UI to reflect the plug status
        log ("updateAttributes", "Power is:" + msg?.json?.Status?.Power, 1)
        if (msg?.json?.Status?.Power == 0){
        	updatePowerState("off")
            }
        else {
        	updatePowerState("on")
            }
        }
    
	//Status 1
    if (msg?.json?.StatusPRM != null){
    	sendEvent(name: "Uptime", value: msg?.json?.StatusPRM?.Uptime, isStateChange: true, displayed: false)
        sendEvent(name: "StartupUTC", value: msg?.json?.StatusPRM?.StartupUTC, isStateChange: true, displayed: false)
        }
    
    //Status 2 - Firmware
	if (msg?.json?.StatusFWR != null){
    	sendEvent(name: "Version", value: msg?.json?.StatusFWR?.Version, isStateChange: true, displayed: false)
        }
    
    //Status 5 - Network
	if (msg?.json?.StatusNET != null){
    	sendEvent(name: "Hostname", value: msg?.json?.StatusNET?.Hostname, isStateChange: true, displayed: false)
        }
    
    //Status 7 - Time
	if (msg?.json?.StatusTIM != null){
    	sendEvent(name: "Local", value: msg?.json?.StatusTIM?.Local, isStateChange: true, displayed: false)
        sendEvent(name: "UTC", value: msg?.json?.StatusTIM?.UTC, isStateChange: true, displayed: false)
        sendEvent(name: "Timezone", value: msg?.json?.StatusTIM?.Timezone, isStateChange: true, displayed: false)
        }
    
    //Status 10 - Sensor
	if (msg?.json?.StatusSNS != null){        
        sendEvent(name: "TotalStartTime", value: msg?.json?.StatusSNS?.ENERGY?.TotalStartTime, isStateChange: true, displayed: false)
        sendEvent(name: "TotalPower", value: msg?.json?.StatusSNS?.ENERGY?.Total, unit: "kWh", isStateChange: true, displayed: false)
        sendEvent(name: "YesterdayPower", value: msg?.json?.StatusSNS?.ENERGY?.Yesterday, unit: "kWh", isStateChange: true, displayed: false)
        sendEvent(name: "TodayPower", value: msg?.json?.StatusSNS?.ENERGY?.Today, unit: "kWh", isStateChange: true, displayed: false)
        sendEvent(name: "Watts", value: msg?.json?.StatusSNS?.ENERGY?.Power, unit: "W", isStateChange: true, displayed: false)
        sendEvent(name: "ApparentPower", value: msg?.json?.StatusSNS?.ENERGY?.ApparentPower, isStateChange: true, displayed: false)
        sendEvent(name: "ReactivePower", value: msg?.json?.StatusSNS?.ENERGY?.ReactivePower, isStateChange: true, displayed: false)
        sendEvent(name: "PowerFactor", value: msg?.json?.StatusSNS?.ENERGY?.Factor, unit: "%", isStateChange: true, displayed: false)
        sendEvent(name: "Voltage", value: msg?.json?.StatusSNS?.ENERGY?.Voltage, unit: "V", isStateChange: true, displayed: false)
        sendEvent(name: "Current", value: msg?.json?.StatusSNS?.ENERGY?.Current, unit: "A", isStateChange: true, displayed: false)
    }      
}

def displayValues(){
	log ("displayValues", "PlugTime:" + device.currentValue("PlugTime") + "...TotalStartTime:" + device.currentValue("TotalStartTime") + "...TotalPower:" + device.currentValue("TotalPower") + "...TodayPower:" + device.currentValue("TodayPower") + "...YesterdayPower:" + device.currentValue("YesterdayPower"), 0 )
    log ("displayValues", "ApparentPower:" + device.currentValue("ApparentPower") + "...ReactivePower:" + device.currentValue("ReactivePower") + "...PowerFactor:" + device.currentValue("PowerFactor"), 0 )
    log ("displayValues", "Watts:" + device.currentValue("Watts") + "...Volts:" + device.currentValue("Volts") + "...Amps:" + device.currentValue("Amps") + "...TimeZone:" + device.currentValue("timeZone"), 0 )
}

//hubAction is an asynchronous response. Not a big deal when the device is operating normally. But when the device is offline and there is no response
//then the parse function never executes leaving us in limbo. By running checkResponse() immediately after callTasmota we can wait to see if the 
//command completed by checking on the value of commandFlag which is cleared if a response is received.
//If a response is not received within 10 seconds then checkResponse will time-out.
def checkResponse(){
	log("checkResponse", "Entering.", 2)
	//Max loop of 20 iterations of 0.5 second each for the 10 second HTTP timeout.
    def x = 0
    def wait = 0
	for (x = 10; x >= 0; x--) {
    	sleepForDuration(1000)
        log("checkResponse", "Processing loop: ${x}", 3)
        log("checkResponse", "CommandFlag is: ${device.currentValue("commandflag")}", 3)
        //If the command has completed we will exit early
        if (device.currentValue("commandflag") == "Complete"){
            //Exit the For loop
            break
        }
        else
        {
        	//Update the status to show we are waiting for a response
            wait = Math.round(x/2)
            //Update the status to show we have received a response. Only do an update on the first change to wait to avoid flooding logs
            if (device.currentValue("status") != "wait") sendEvent(name:"status", value: "wait", displayed:true, isStateChange:true)
        }
    }
    //If we get this far it means that either the command was complete or it is still Processing\timed out.
    def mytime = elapsedTime()
    def boolean result = false
    log("checkResponse", "CommandFlag is: ${device.currentValue("commandflag")}", 1)
    switch(device.currentValue("commandflag")) { 
        case ["Complete"]: 
        log("checkResponse", "Success...${mytime} seconds" , 0)
        //Update the status to show we have received a response
        sendEvent(name:"status", value: "success", displayed:true, isStateChange:true)
        result = true
        break

        case ["Processing"]: 
        log("checkResponse", "Fail-Timeout" , -1)
        //After 10 seconds of processing we can consider the request timed out\failed.
        sendEvent(name: "commandflag", value: "Failed...${mytime} seconds", displayed:false)
        //Update the status to show we have NOT received a response
        sendEvent(name:"status", value: "fail", displayed:true, isStateChange:true)
        result = false
        break;
    }
    log("checkResponse", "Exiting with: ${result}" , 1)
    return result
}


//End of main program section
//*********************************************************************************************************************************************************************


//*********************************************************************************************************************************************************************
// Start of Supporting functions
private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

//Updates the device network information - Allows user to force an update of the device network information if required.
def updateDeviceNetworkID() {
	def newline = "\n"
    def tab = "\t"
    
    try{
    	log("updateDeviceNetworkID", "Settings are:" + settings.destIp + ":" + settings.destPort, 1)
        def hosthex = convertIPtoHex(settings.destIp)
        def porthex = convertPortToHex(settings.destPort)
    	def desireddni = "$hosthex:$porthex"
        
        def actualdni = device.deviceNetworkId
        
        //If they don't match then we need to update the DNI
        if (desireddni !=  actualdni){
        	device.deviceNetworkId = "$hosthex:$porthex" 
            log("Action", "Save updated DNI: ${"$hosthex:$porthex"}", 0)
         	}
        else
        	{
            log("Action", "DNI: ${"$hosthex:$porthex"} is correct. Not updated. ", 0)
            }
        sendEvent(name: "dni", value: device.deviceNetworkId)
        }
    catch (e){
    	log("Save", "Error updating Device Network ID: ${e}", -1)
     	}
}

//Sleeps the process for duration seconds in a non blocking manner.
//Used to wait after a hubAction request to pause the thread execution and see if there is any response.
def sleepForDuration(msDuration)
{
	def dTotalSleep = 0
	def dStart = new Date().getTime()
    def cmds = []
	cmds << "delay 1"   
    while (dTotalSleep <= msDuration)
    {            
		cmds
        dTotalSleep = (new Date().getTime() - dStart)
    }
    log("sleepForDuration", "Slept ${dTotalSleep}ms", 3)
}

//Returns the elapsed time since the command was originally issued
def elapsedTime(){
    def strlastcommandtime = device.currentValue("commandtime")
    long lastcommandtime = Long.parseLong(strlastcommandtime)
    def timedifference = (now() - lastcommandtime)/1000
    return timedifference
}

//Returns the elapsed time since the "since" time
def timeSince(long since){
    def timedifference = (now()-since)/1000
    return timedifference.toInteger()
}

//Creates a map for easy access to formatted time data for the local TimeZone
def formatTime(value){
	def Long valueLong = value
    Date valueDate = new Date(valueLong)
	def String valueString = valueDate.toString()          
    String datePart = valueDate.format("MM/dd", location.timeZone)
	String timePart = valueDate.format("HH:mm:ss", location.timeZone)
    String dayPart = valueDate.format("EEE", location.timeZone)
    return [timeLong: valueLong, timeString: valueString, timeDate: datePart, timeTime: timePart, timeDay: dayPart]
}

//Creates a map for easy access to contents of the Tasmota StatusTIM time strings
def tasmotaTimeToMap(msg){
	def int first = msg.indexOf(" ")
	def int second = msg.indexOf(" ", first + 1)
    def int third = msg.indexOf(" ", second + 1)
    def int fourth = msg.indexOf(" ", third + 1)
    
    def valueDay = msg.substring(0, first)
    def valueMonth = msg.substring(first+1, second)
    def valueDate = msg.substring(second+1, third)
    def valueTime = msg.substring(third+1, fourth)
    def valueYear = msg.substring(fourth)
    
    return [Day: valueDay, Month: valueMonth, Date: valueDate, Time: valueTime, Year: valueYear]
}

//Creates a map for easy access to contents of Groovy time strings
def groovyTimeToMap(msg){
	def int first = msg.indexOf(" ")
    def int second = msg.indexOf(" ", first + 1)
    def int third = msg.indexOf(" ", second + 1)
    def int fourth = msg.indexOf(" ", third + 1)
    def int fifth = msg.indexOf(" ", fourth + 1)
    def int sixth = msg.indexOf(" ", fifth)
    
    def valueDay = msg.substring(0, first)
    def valueMonth = msg.substring(first+1, second)
    def valueDate = msg.substring(second+1, third)
    def valueTime = msg.substring(third+1, fourth)
    def valueTimezone = msg.substring(fourth+1, fifth)
    def valueYear = msg.substring(sixth)
    
    return [Day: valueDay, Month: valueMonth, Date: valueDate, Time: valueTime, Timezone: valueTimezone, Year: valueYear]
}


//Selects the state of the Wifi tile based on the Wifi signal strength information received from Tasmota
private setWifi(signal){
	log("Wifi", "Signal is ${signal}", 2)
    int RSSI = signal.toInteger()
	  switch(RSSI) { 
      		case 101:                
            	//Special case we can call when needed when we don't know the Wifi state.
                sendEvent(name:"wifi", value: "Unknown", isStateChange: true)
                break
            
			case 75..100:                
                sendEvent(name:"wifi", value: "High", isStateChange: true)
                break

            case 45..74: 
                sendEvent(name:"wifi", value: "Medium", isStateChange: true)
                break

            case 1..44: 
                sendEvent(name:"wifi", value: "Low", isStateChange: true)
                break

            case 0: 
                sendEvent(name:"wifi", value: "None", isStateChange: true)
                break;
       }
}

// End of Supporting functions
//*********************************************************************************************************************************************************************



//*********************************************************************************************************************************************************************
//Start of Future development section - no calls are made to these functions at this time

    	
//End of Future development section
//*********************************************************************************************************************************************************************