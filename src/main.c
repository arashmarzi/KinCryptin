#include <pebble.h>
#define TIMESTAMP_LOG 1
static Window *my_window;
static TextLayer *text_layers;

static Layer *s_layer;

// Accelerometer variables
static uint32_t num_samples = 1; // number of samples per batch/callback

// Data logging variables
static DataLoggingResult data_logging_result;
static DataLoggingSessionRef s_session_ref; // The session reference variables

static void log_data(AccelData *data, uint32_t num_samples) { 
  data_logging_result = data_logging_log(s_session_ref, &data, num_samples);
  
  if(data_logging_result != DATA_LOGGING_SUCCESS) {
    APP_LOG(APP_LOG_LEVEL_ERROR, "Error logging data: %d", (int)data_logging_result);
  }
}

static void accel_data_handler(AccelData *data, uint32_t num_samples) {
  // Read sample 0's x, y, and z values
  int16_t x = data[0].x;
  int16_t y = data[0].y;
  int16_t z = data[0].z;

  // Determine if the sample occured during vibration, and when it occured
  bool did_vibrate = data[0].did_vibrate;
  uint64_t timestamp = data[0].timestamp;
  
  if(!did_vibrate) {
    // Print it out   
    log_data(data, num_samples);
    APP_LOG(APP_LOG_LEVEL_INFO, "t: %llu, x: %d, y: %d, z: %d",
                                                          timestamp, x, y, z);
  } else {
    // Discard with a warning
    APP_LOG(APP_LOG_LEVEL_WARNING, "Vibration occured during collection");
  }
}

static void handle_init(void) {
  my_window = window_create();
  window_set_background_color(my_window, GColorWhite);
  
  text_layers = text_layer_create(GRect(0, 0, 144, 20));
  window_stack_push(my_window, true);  
  
  accel_service_set_sampling_rate(ACCEL_SAMPLING_50HZ);  
  accel_data_service_subscribe(num_samples, accel_data_handler);  
  
  // Begin the data logging session
  s_session_ref = data_logging_create(TIMESTAMP_LOG, DATA_LOGGING_INT, sizeof(int), true);
}

static void handle_deinit(void) {   
  // Finish the session and sync data if appropriate
  data_logging_finish(s_session_ref);
  
  accel_data_service_unsubscribe();
  
  text_layer_destroy(text_layers);
  window_destroy(my_window);
}

int main(void) {
  handle_init();
  APP_LOG(APP_LOG_LEVEL_INFO, "exiting handle_init");
  app_event_loop();
  APP_LOG(APP_LOG_LEVEL_INFO, "exiting app_event_loop");
  handle_deinit();
  APP_LOG(APP_LOG_LEVEL_INFO, "exiting handle_deinit");
}
