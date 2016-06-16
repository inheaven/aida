// vssa.cpp: определяет экспортированные функции для приложения DLL.
//

//#define NOMINMAX

#include "stdafx.h"
#include <iostream>
#include <cstdlib>


#include <boost/numeric/ublas/matrix.hpp>

#define VIENNACL_WITH_UBLAS 1
#define VIENNACL_WITH_OPENCL 1
#define VIENNACL_DEBUG_ALL 1

#include "viennacl/ocl/device.hpp"
#include "viennacl/ocl/platform.hpp"
#include "viennacl/ocl/backend.hpp"
#include "viennacl/device_specific/builtin_database/common.hpp"

#include "viennacl/scalar.hpp"
#include "viennacl/scalar.hpp"
#include "viennacl/matrix.hpp"
#include "viennacl/linalg/prod.hpp"  

#include <algorithm>

using namespace boost::numeric::ublas;

int main() {
	std::cout << "Hello DLL" << std::endl;
	
	viennacl::ocl::platform pf = viennacl::ocl::get_platforms()[0];

	std::vector<viennacl::ocl::device> const & devices = pf.devices(CL_DEVICE_TYPE_GPU);
	for (int i = 0; i < devices.size(); ++i) {
		viennacl::ocl::setup_context(0, devices[i]);
	}			

	test();	
		
	return 0;
}

void vssa() {
	int n = 255;
	int l = 128;
	int p = 9;

	viennacl::scalar<float> k = n - l + 1;
	viennacl::scalar<float> ld = l - 1;

	viennacl::matrix<float> x(l, k);
	viennacl::matrix<float> xi(l, k);
	viennacl::matrix<float> g(n, p);
	viennacl::vector<float> s(l);
}

void test() {
	viennacl::ocl::set_context_device_type(0, viennacl::ocl::gpu_tag());

	matrix<float> matrix(10000, 10000);

	for (unsigned int i = 0; i < matrix.size1(); ++i)
		for (unsigned int j = 0; j < matrix.size2(); ++j)
			matrix(i, j) = i*j;

	viennacl::matrix<float> m = viennacl::zero_matrix<float>(10000, 10000);
		
	viennacl::fast_copy(matrix.data().begin(), matrix.data().end(), m);

	viennacl::scalar<float> x = 2;

	m *= x; 

	m *= 2;
}

void info() {
	typedef std::vector< viennacl::ocl::platform > platforms_type;
	platforms_type platforms = viennacl::ocl::get_platforms();

	bool is_first_element = true;
	for (platforms_type::iterator platform_iter = platforms.begin();
		platform_iter != platforms.end();
		++platform_iter)
	{
		typedef std::vector<viennacl::ocl::device> devices_type;
		devices_type devices = platform_iter->devices(CL_DEVICE_TYPE_ALL);

		/**
		*  Print some platform information
		**/
		std::cout << "# =========================================" << std::endl;
		std::cout << "#         Platform Information             " << std::endl;
		std::cout << "# =========================================" << std::endl;

		std::cout << "#" << std::endl;
		std::cout << "# Vendor and version: " << platform_iter->info() << std::endl;
		std::cout << "#" << std::endl;

		if (is_first_element)
		{
			std::cout << "# ViennaCL uses this OpenCL platform by default." << std::endl;
			is_first_element = false;
		}


		/**
		*  Traverse the devices and print all information available using the convenience member function full_info():
		**/
		std::cout << "# " << std::endl;
		std::cout << "# Available Devices: " << std::endl;
		std::cout << "# " << std::endl;
		for (devices_type::iterator iter = devices.begin(); iter != devices.end(); iter++)
		{
			std::cout << std::endl;

			std::cout << "  -----------------------------------------" << std::endl;
			std::cout << iter->full_info();
			std::cout << "ViennaCL Device Architecture:  " << iter->architecture_family() << std::endl;
			std::cout << "ViennaCL Database Mapped Name: " << viennacl::device_specific::builtin_database::get_mapped_device_name(iter->name(), iter->vendor_id()) << std::endl;
			std::cout << "  -----------------------------------------" << std::endl;
		}
		std::cout << std::endl;
		std::cout << "###########################################" << std::endl;
		std::cout << std::endl;
	}
}


