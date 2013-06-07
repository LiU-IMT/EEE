/* 
 * From: https://github.com/redbeard-tech/raphael_glow_shadow/
 * 
 * Glow and Shadow 0.1 - Glow and Shadow plugin for Raphael.js
 *
 * Copyright (c) 2010 RedBeard Tech (http://www.redbeard-tech.com)
 * Licensed under the MIT license.
 */

Raphael.el.glow = function(size, color, opacity) {
  if (typeof(opacity) == 'undefined') opacity = 0.75;
  if (typeof(this.glowSet) == 'undefined') {
    this.glowSet = this.paper.set();
  } else { 
    while (this.glowSet.length > 0) { this.glowSet.pop().remove(); }
  }
  for (i = 0; i < size; i++) {
    switch(this.type) {
      case 'rect': new_element = this.paper.rect(this.attr('x') - size + i, this.attr('y') - size + i, this.attr('width') + (size * 2) - (i * 2), this.attr('height') + (size * 2) - (i * 2), this.attr('r') + size - i); break;
      case 'circle': new_element = this.paper.circle(this.attr('cx'), this.attr('cy'), this.attr('r') + size - i); break;
      case 'ellipse': new_element = this.paper.ellipse(this.attr('cx'), this.attr('cy'), this.attr('rx') + size - i, this.attr('ry') + size - i); break;
      default: throw "Glow and Shadow does not support the " + this.type + " Raphael element!"; break;
    }
    this.glowSet.push(new_element.attr({
      opacity : opacity - ((size - i) * ((opacity - .1) / size))
    }));
  }
  this.onAnimation(function() {
    this.glow(color, size, opacity);
  });
  return this.glowSet.attr({
    fill: color,
    'stroke-width' : 0
  }).insertBefore(this);
}

Raphael.el.shadow = function (x_offset, y_offset, size, color) {
  if (typeof(this.shadowSet) == 'undefined') {
    this.shadowSet = this.paper.set();
  } else { 
    while (this.shadowSet.length > 0) { this.shadowSet.pop().remove(); }
  }
  switch(this.type) {
    case 'rect': 
      var width = this.attr('width'),
          height = this.attr('height'),
          left = this.attr('x'),
          top = this.attr('y'),
          radialGradient = Raphael.format("r{0}-{0}", color);
          
      this.shadowSet.push(
        this.paper.rect(left + x_offset - size, top + y_offset, size, height).attr({stroke: 'none', fill: Raphael.format("180-{0}-{0}", color), opacity: 0, "clip-rect": [left + x_offset - size, top + y_offset, size, height]}),
        this.paper.rect(left + x_offset, top + y_offset + height, width, size).attr({stroke: 'none', fill: Raphael.format("270-{0}-{0}", color), opacity: 0, "clip-rect": [left + x_offset, top + y_offset + height, width, size]}),
        this.paper.rect(left + x_offset + width, top + y_offset, size, height).attr({stroke: 'none', fill: Raphael.format("0-{0}-{0}", color), opacity: 0, "clip-rect": [left + x_offset + width, top + y_offset, size, height]}),
        this.paper.rect(left + x_offset, top + y_offset - size, width, size).attr({stroke: 'none', fill: Raphael.format("90-{0}-{0}", color), opacity: 0, "clip-rect": [left + x_offset, top + y_offset - size, width, size]}),
        this.paper.circle(left + x_offset, top + y_offset, size).attr({stroke: 'none', fill: radialGradient, opacity: 0, "clip-rect": [left + x_offset - size, top + y_offset - size, size, size]}),
        this.paper.circle(left + x_offset, top + y_offset + height, size).attr({stroke: 'none', fill: radialGradient, opacity: 0, "clip-rect": [left + x_offset - size, top + y_offset + height, size, size]}),
        this.paper.circle(left + x_offset + width, top + y_offset + height, size).attr({stroke: 'none', fill: radialGradient, opacity: 0, "clip-rect": [left + x_offset + width, top + y_offset + height, size, size]}),
        this.paper.circle(left + x_offset + width, top + y_offset, size).attr({stroke: 'none', fill: radialGradient, opacity: 0, "clip-rect": [left + x_offset + width, top + y_offset - size, size, size]}),
        this.paper.rect(left + x_offset, top + y_offset, width, height).attr({fill: color, stroke: 'none'})
      );
      break;
    case 'circle':
      var radius = this.attr('r'),
          left = this.attr('cx'),
          top = this.attr('cy'),
          radialGradient = Raphael.format("r{0}:{1}-{0}", color, 100 - ((size / radius) * 100));
      
      this.shadowSet.push(
        this.paper.circle(left + x_offset, top + y_offset, size + radius).attr({stroke: 'none', fill: radialGradient, opacity: 0})
      );
      break;
    case 'ellipse':
      var radius_x = this.attr('rx'),
          radius_y = this.attr('ry'),
          left = this.attr('cx'),
          top = this.attr('cy'),
          radialGradient = Raphael.format("r{0}:{1}-{0}", color, 100 - ((size / radius_x) * 100));
      
      this.shadowSet.push(
        this.paper.ellipse(left + x_offset, top + y_offset, size + radius_x, size + radius_y).attr({stroke: 'none', fill: radialGradient, opacity: 0})
      );
      break;
    default: throw "Glow and Shadow does not support the " + this.type + " Raphael element!"; break;
  }
  this.onAnimation(function() {
    this.shadow(x_offset, y_offset, size, color);
  });
  return this.shadowSet.insertBefore(this);
};