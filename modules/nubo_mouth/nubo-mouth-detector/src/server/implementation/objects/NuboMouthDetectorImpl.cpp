/* Autogenerated with kurento-module-creator */

#include <gst/gst.h>
#include <vector>
#include <MouthInfo.hpp> 
#include "MediaPipeline.hpp"
#include "MediaPipelineImpl.hpp"
#include <NuboMouthDetectorImplFactory.hpp>
#include "NuboMouthDetectorImpl.hpp"
#include <jsonrpc/JsonSerializer.hpp>
#include <KurentoException.hpp>
#include <SignalHandler.hpp>

#define GST_CAT_DEFAULT kurento_nubo_mouth_detector_impl
GST_DEBUG_CATEGORY_STATIC (GST_CAT_DEFAULT);
#define GST_DEFAULT_NAME "KurentoNuboMouthDetectorImpl"

#define VIEW_MOUTHS "view-mouths"
#define DETECT_BY_EVENT "detect-event"
#define SEND_META_DATA "send-meta-data"
#define FACTOR_SCALE "multi-scale-factor"
#define X_EVERY_4_FRAMES "process-x-every-4-frames"
#define WIDTH_TO_PROCESS "width-to-process"
#define ACTIVATE_EVENTS "activate-events"
#define EVENTS_MS "events-ms"

namespace kurento
{
  namespace module
  {
    namespace nubomouthdetector
    {


      void  NuboMouthDetectorImpl::split_message (std::string fi, std::string delimiter, 
					     std::vector<std::string> *v)
      {
	size_t pos = 0;
	std::string token;
	
	while ((pos = fi.find(delimiter)) != std::string::npos) {
	  token = fi.substr(0, pos);  
	  v->push_back(token);
	  fi.erase(0, pos + delimiter.length());
	}
	
	v->push_back(fi);
	
      }

      
      void NuboMouthDetectorImpl::onMouth (gchar *message)
      {

	/*the String received will be like this ( the ; is the seperation among mouths): 
	  x:int,y:int,width:int,height:int;x:int,y:int,width:int,height:int;*/
	std::string del1 = ";";
	std::string del2 = ",";
	std::string del3 = ":";
	std::vector<std::string> *mouths = new std::vector<std::string>;
	std::vector<std::string> *fields = new std::vector<std::string>;
	std::vector<std::string> *all = new std::vector<std::string>;
	std::vector<std::shared_ptr<MouthInfo>> test;
	int x,y,height,width;
	std::string t;
  
	bool register_completed=true;
	int i=0;
  
	try {
    
	  split_message(message,del1,mouths);
	  for( i=0; (int)(mouths->size())>i; i++) 
	    split_message(mouths->at(i),del2,fields);
    
	  for( i=0; (int)(fields->size())>i; i++)
	    split_message(fields->at(i),del3,all);   
    
	  for(int i=0; (int)(all->size())>i; i=i+2)
	    {
	
	      if (register_completed)
		{
		  t="mouth";
		  x=0;y=0;width=0;height=0;
		  register_completed=false;
		}
	      if (0==all->at(i).compare("x")) 	
		{
		  if ((i+1)< (int)(all->size()))	      
		    x=std::stoi(all->at(i+1));
		}
	      else if (0==all->at(i).compare("y")) 
		{
		  if ((i+1)< (int)(all->size()))
		    y=std::stoi(all->at(i+1));
		}
	
	      else if (0==all->at(i).compare("width"))     
		{
		  if ((i+1)< (int)(all->size()))
 		    width=std::stoi(all->at(i+1));
		}
	      else if (0==all->at(i).compare("height"))     
		{
		  if ((i+1)< (int)(all->size()))
		    {
		      height=std::stoi(all->at(i+1));
		      std::shared_ptr <MouthInfo> cand ( new MouthInfo(t,x,y,width,height));
		      test.push_back(cand);
		      register_completed=true;
		    }
		}
	    }

   	  if (test.size()>0)
	    {
	      
	      OnMouth event (shared_from_this(), OnMouth::getName(), test);
	      signalOnMouth (event);
	    }
	} catch (std::bad_weak_ptr &e) {
	}
      }


      void NuboMouthDetectorImpl::postConstructor ()
      {
	handlerOnMouthEvent = register_signal_handler (G_OBJECT (nubo_mouth),
			      "mouth-event",
			       std::function <void (GstElement *, gchar *) >
			       (std::bind (&NuboMouthDetectorImpl::onMouth, this,
			        std::placeholders::_2) ),
			       std::dynamic_pointer_cast<NuboMouthDetectorImpl>
			      (shared_from_this() ) );
      }

      NuboMouthDetectorImpl::NuboMouthDetectorImpl (const boost::property_tree::ptree &config, std::shared_ptr<MediaPipeline> mediaPipeline)  : FilterImpl (config, std::dynamic_pointer_cast<MediaPipelineImpl> (mediaPipeline)) 
      {
  
	g_object_set (element, "filter-factory", "nubomouthdetector", NULL);

	g_object_get (G_OBJECT (element), "filter", &nubo_mouth, NULL);

	if (NULL == nubo_mouth) {
	  throw KurentoException (MEDIA_OBJECT_NOT_AVAILABLE,
				  "Media Object not available");
	}

	g_object_unref(nubo_mouth);

      }

      void NuboMouthDetectorImpl::showMouths(int viewMouths)
      {
	g_object_set(G_OBJECT (nubo_mouth), VIEW_MOUTHS, viewMouths, NULL);
      }

      void NuboMouthDetectorImpl::detectByEvent(int event)
      {
  
	g_object_set(G_OBJECT (nubo_mouth), DETECT_BY_EVENT, event, NULL);
  
      }
  
      void NuboMouthDetectorImpl::sendMetaData(int metaData)
      {
	g_object_set(G_OBJECT (nubo_mouth),SEND_META_DATA , metaData, NULL);
      }

      void NuboMouthDetectorImpl::multiScaleFactor(int scaleFactor)
      {
	printf("NuboMouthDetectorImpl.cpp receive scale Factor %d \n",scaleFactor);
	g_object_set(G_OBJECT (nubo_mouth),FACTOR_SCALE , scaleFactor, NULL);
      }

      void NuboMouthDetectorImpl::processXevery4Frames(int xper4)
      {
	printf("NuboMouthDetectorImpl.cpp receive fps  %d \n",xper4);
	g_object_set(G_OBJECT (nubo_mouth),X_EVERY_4_FRAMES , xper4, NULL);
      }

      void NuboMouthDetectorImpl::widthToProcess(int width)
      {

	g_object_set(G_OBJECT (nubo_mouth),WIDTH_TO_PROCESS , width, NULL);
      }

      void NuboMouthDetectorImpl::activateServerEvents (int activate,int ms)
      {
	g_object_set (G_OBJECT (nubo_mouth), ACTIVATE_EVENTS , activate, NULL);
	g_object_set (G_OBJECT (nubo_mouth), EVENTS_MS , ms , NULL);
      }

      MediaObjectImpl *
      NuboMouthDetectorImplFactory::createObject (const boost::property_tree::ptree &config, std::shared_ptr<MediaPipeline> mediaPipeline) const
      {

	return new NuboMouthDetectorImpl (config, mediaPipeline);
      }


      NuboMouthDetectorImpl::StaticConstructor NuboMouthDetectorImpl::staticConstructor;

      NuboMouthDetectorImpl::StaticConstructor::StaticConstructor()
      {
	GST_DEBUG_CATEGORY_INIT (GST_CAT_DEFAULT, GST_DEFAULT_NAME, 0,
				 GST_DEFAULT_NAME);
      }

      NuboMouthDetectorImpl::~NuboMouthDetectorImpl()
      {
	if ( handlerOnMouthEvent > 0) {
	  unregister_signal_handler (nubo_mouth, handlerOnMouthEvent );
	}
      }

    } /* nubomouthdetector */
  } /* module */
} /* kurento */
