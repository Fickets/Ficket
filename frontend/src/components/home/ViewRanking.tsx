import { useState, useEffect } from 'react';
import PCViewRanking from './PCViewRanking';
import MobileViewRanking from './MobileViewRanking';

const ViewRanking = () => {
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.matchMedia('(max-width: 768px)').matches);
    };

    handleResize(); // Initialize on mount
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  return isMobile ? <MobileViewRanking /> : <PCViewRanking />;
};

export default ViewRanking;
