
/* Autogenerated with kurento-module-creator */

#ifndef __NUBO_EYE_DETECTOR_IMPL_HPP__
#define __NUBO_EYE_DETECTOR_IMPL_HPP__

#include "FilterImpl.hpp"
#include "NuboEyeDetector.hpp"
#include <EventHandler.hpp>
#include <boost/property_tree/ptree.hpp>

namespace kurento
{
  namespace module
  {
    namespace nuboeyedetector
    {
      class NuboEyeDetectorImpl;
    } /* nuboeyedetector */
  } /* module */
} /* kurento */

namespace kurento
{
  void Serialize (std::shared_ptr<kurento::module::nuboeyedetector::NuboEyeDetectorImpl> &object, JsonSerializer &serializer);
} /* kurento */

namespace kurento
{
  class MediaPipelineImpl;
} /* kurento */

namespace kurento
{
  namespace module
  {
    namespace nuboeyedetector
    {

      class NuboEyeDetectorImpl : public FilterImpl, public virtual NuboEyeDetector
      {

      public:

	NuboEyeDetectorImpl (const boost::property_tree::ptree &config, std::shared_ptr<MediaPipeline> mediaPipeline);

	virtual ~NuboEyeDetectorImpl ();

	void showEyes (int viewEyes);
	void detectByEvent(int event);
	void sendMetaData(int metaData);
	void multiScaleFactor(int scaleFactor);
	void processXevery4Frames(int xper4);
	void widthToProcess(int width);
	void activateServerEvents (int activate,int ms);

	sigc::signal<void, OnEye> signalOnEye;
	/* Next methods are automatically implemented by code generator */
	virtual bool connect (const std::string &eventType, std::shared_ptr<EventHandler> handler);
	virtual void invoke (std::shared_ptr<MediaObjectImpl> obj,
			     const std::string &methodName, const Json::Value &params,
			     Json::Value &response);
	
	virtual void Serialize (JsonSerializer &serializer);

      protected: 
	virtual void postConstructor ();

      private:

	GstElement *nubo_eye = NULL;
	gulong handlerOnEyeEvent = 0;
	void onEye (gchar *);
	void split_message (std::string fi, std::string delimiter, std::vector<std::string> *v);
	

	class StaticConstructor
	{
	public:
	  StaticConstructor();
	};

	static StaticConstructor staticConstructor;

      };

    } /* nuboeyedetector */
  } /* module */
} /* kurento */

#endif /*  __NUBO_EYE_DETECTOR_IMPL_HPP__ */
